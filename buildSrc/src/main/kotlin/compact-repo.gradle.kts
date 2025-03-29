
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText


open class CompactingResourcesExtension {

    internal val compactors: MutableList<CompactedResources> = mutableListOf()
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
project.extensions.create<CompactingResourcesExtension>("compacting-resource")

class CompactToObject(val folder: String, val outputFile: String) : CompactedResources {
    var jsonObject: JsonObject? = null
    override fun getPath() = arrayOf("$folder/**")
    override fun setup() {
        jsonObject = JsonObject()
    }

    override fun add(fileName: String, element: JsonElement) {
        jsonObject!!.add(fileName, element)
    }

    override fun complete(): JsonElement {
        val value = jsonObject!!
        jsonObject = null
        return value
    }

    override fun getOutput() = outputFile
}

class CompactToArray(val folder: String, val outputFile: String) : CompactedResources {
    var jsonArray: JsonArray? = null
    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")
    override fun setup() {
        jsonArray = JsonArray()
    }

    override fun add(fileName: String, element: JsonElement) {
        jsonArray!!.add(element)
    }

    override fun complete(): JsonElement {
        val value = jsonArray!!
        this.jsonArray = null
        return value
    }

    override fun getOutput() = outputFile
}

interface CompactedResources {
    fun getPath(): Array<String>
    fun setup()
    fun add(fileName: String, element: JsonElement)
    fun complete(): JsonElement
    fun getOutput(): String
}

data class ExternalResource(val url: String, val name: String, val json: Boolean)

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
            logger.warn("Downloading {} from {}", resource.name, resource.url)
            val openStream = URI(resource.url).toURL().openStream()
            openStream.use {
                val contents = openStream.readAllBytes().toString(Charsets.UTF_8)
                val output: String = if (resource.json) {
                    JsonParser.parseString(contents).toString()
                } else {
                    contents
                }
                val path = outputBaseDirectory.resolve(resource.name)
                path.parent.createDirectories()
                path.writeText(output, options = arrayOf(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
            }
        }

        configuration.compactors.forEach { compactor ->
            compactor.setup()
            val pathsToCompact = compactor.getPath().map { "${configuration.basePath}/$it" }
            logger.warn("Compacting folder {}", compactor.getOutput())

            directoriesToSearch.forEach { file ->
                fileTree(file) {

                    include(*pathsToCompact.toTypedArray())
                    exclude(*mutableListOf(*listOfPaths.toTypedArray()).apply { this.removeAll(pathsToCompact) }.toTypedArray())

                    forEach {
                        compactor.add(it.nameWithoutExtension, JsonParser.parseString(it.readText()))
                    }
                }
            }

            val complete = compactor.complete()
            val output = compactor.getOutput()

            outputBaseDirectory.createDirectories()
            outputBaseDirectory.resolve("$output.json").writeText(
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
