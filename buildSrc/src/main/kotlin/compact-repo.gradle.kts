
import com.google.gson.JsonParser
import me.owdding.skyblockpv.museum.CreateMuseumDataTask
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.hours

project.extensions.create<CompactingResourcesExtension>("compactingResource")

val downloadCache = FileCache(project.gradle.gradleUserHomeDir.toPath().resolve(DEFAULT_CACHE_DIRECTORY), 1.hours)
val museumDataTask = tasks.register<CreateMuseumDataTask>("createMuseumData")

tasks.withType<ProcessResources>().configureEach {
    dependsOn(museumDataTask.get())
    outputs.upToDateWhen { false }
    val configuration = project.extensions.getByType<CompactingResourcesExtension>()
    val sourceSets = project.extensions.getByType<SourceSetContainer>()
    val listOfPaths = configuration.compactors.flatMap { it.getPath().toList() }.map { "${configuration.basePath}/$it" }

    val outDirectory = project.layout.buildDirectory.file("generated/compacted_resources/").get().asFile.toPath()
    outDirectory.parent.createDirectories()
    val outputBaseDirectory = outDirectory.resolve(configuration.basePath!!)
    val projectDirectory = layout.projectDirectory.asFile.toPath()

    exclude { projectDirectory.relativize(it.file.toPath()).toString().contains("src/main/resources/${configuration.basePath}") }
    from(project.layout.buildDirectory.dir("generated/compacted_resources/").get())
    from(museumDataTask.get().outputs.files)

    sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.add(outDirectory.toFile())
    val task = this

    doFirst {
        val directoriesToSearch = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.map { it.toPath() }
            .toMutableList().apply { add(outDirectory) }

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
                println("Searching $file")
                fileTree(file) {

                    include(*pathsToCompact.toTypedArray())
                    exclude(*listOfPaths.toMutableList().apply { this.removeAll(pathsToCompact) }.toTypedArray())

                    forEach {
                        task.exclude(file.relativize(it.toPath()).toString())
                        println("Excluding ${file.relativize(it.toPath())}")
                        compactor.add(it.nameWithoutExtension, JsonParser.parseString(it.readText()))
                    }
                }
            }

            val complete = compactor.complete()

            val resolve = outputBaseDirectory.resolve("${compactor.output}.json")
            resolve.parent.createDirectories()
            resolve.writeText(
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
