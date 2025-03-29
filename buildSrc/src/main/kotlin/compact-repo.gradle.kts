
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes

open class CompactingResourcesExtension {

    internal val compactors: MutableList<CompactedResources<*>> = mutableListOf()
    internal val externalResources: MutableList<ExternalResource> = mutableListOf()
    var basePath: String? = null

    fun compactToArray(folder: String, output: String = folder) {
        compactors.add(CompactToArray(folder, output))
    }

    fun compactToObject(folder: String, output: String = folder) {
        compactors.add(CompactToObject(folder, output))
    }

    fun downloadResource(url: String, output: String, json: Boolean = true) {
        externalResources.add(ExternalResource(url, output, json))
    }
}
project.extensions.create<CompactingResourcesExtension>("compactingResource")

class CompactToObject(val folder: String, val outputFile: String) : CompactedResources<JsonObject>(::JsonObject, outputFile) {
    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")

    override fun add(fileName: String, element: JsonElement) {
        value!!.add(fileName, element)
    }
}
class CompactToArray(private val folder: String, outputFile: String) : CompactedResources<JsonArray>(::JsonArray, outputFile) {
    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")

    override fun add(fileName: String, element: JsonElement) {
        value!!.add(element)
    }
}

abstract class CompactedResources<T: JsonElement>(private val factory: () -> T, val output: String) {
    protected var value: T? = null

    abstract fun add(fileName: String, element: JsonElement)
    abstract fun getPath(): Array<String>

    fun setup() {
        value = factory()
    }

    fun complete(): JsonElement {
        val data = value!!
        value = null
        return data
    }
}

data class ExternalResource(val url: String, val name: String, val json: Boolean)

val downloadCache = DownloadedFileCache(project.gradle.gradleUserHomeDir.toPath().resolve("caches/sbpv_download"), 30.minutes)

tasks.withType<ProcessResources>().configureEach {
    val configuration = project.extensions.getByType<CompactingResourcesExtension>()
    val sourceSets = project.extensions.getByType<SourceSetContainer>()
    val listOfPaths = configuration.compactors.flatMap { it.getPath().toList() }.map { "${configuration.basePath}/$it" }

    val outDirectory = project.layout.buildDirectory.file("generated/compacted_resources/").get().asFile.toPath()
    outDirectory.parent.createDirectories()
    val outputBaseDirectory = outDirectory.resolve(configuration.basePath!!)
    val projectDirectory = layout.projectDirectory.asFile.toPath()

    exclude { projectDirectory.relativize(it.file.toPath()).toString().contains("src/main/resources/${configuration.basePath}") }
    from(project.layout.buildDirectory.dir("generated/compacted_resources/").get())

    sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.add(outDirectory.toFile())

    doFirst {
        val directoriesToSearch = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.map { it.toPath() }

        configuration.externalResources.forEach { resource ->
            val orDownload = downloadCache.getOrDownload(resource.url)

            val contents = orDownload.toString(Charsets.UTF_8)
            val output: String = if (resource.json) {
                JsonParser.parseString(contents).toString()
            } else {
                contents
            }
            val path = outputBaseDirectory.resolve(resource.name)
            path.parent.createDirectories()
            path.writeText(output, options = arrayOf(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
        }

        configuration.compactors.forEach { compactor ->
            compactor.setup()
            val pathsToCompact = compactor.getPath().map { "${configuration.basePath}/$it" }
            logger.warn("Compacting folder {}", compactor.output)

            directoriesToSearch.forEach { file ->
                fileTree(file) {

                    include(*pathsToCompact.toTypedArray())
                    exclude(*listOfPaths.toMutableList().apply { this.removeAll(pathsToCompact) }.toTypedArray())

                    forEach {
                        compactor.add(it.nameWithoutExtension, JsonParser.parseString(it.readText()))
                    }
                }
            }

            val complete = compactor.complete()

            outputBaseDirectory.createDirectories()
            outputBaseDirectory.resolve("${compactor.output}.json").writeText(
                complete.toString(),
                options = arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
            )
        }

        directoriesToSearch.forEach { file ->
            fileTree(file) {
                include(configuration.basePath?.let { "$it/**" } ?: "")
                exclude(*listOfPaths.toTypedArray())
                forEach {
                    val toRelativeString = it.toRelativeString(file.resolve(configuration.basePath!!).toFile())
                    logger.warn("Compacting file {}", toRelativeString)
                    val parseString = JsonParser.parseString(it.readText())
                    val path = outputBaseDirectory.resolve(toRelativeString)
                    path.parent.createDirectories()
                    path.writeText(
                        parseString.toString(),
                        options = arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                    )
                }
            }
        }
    }
}
