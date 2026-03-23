import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.ValidateAccessWidenerTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("idea")
    id("museum-data")
    id("versioned-catalogues")
    kotlin("jvm")
    id("me.owdding.auto-mixins")
    id("me.owdding.resources")
    id("com.google.devtools.ksp")
}

private val stonecutter = project.extensions.getByName("stonecutter") as dev.kikugie.stonecutter.build.StonecutterBuildExtension
fun isUnobfuscated() = stonecutter.eval(stonecutter.current.version, ">=26.1")

repositories {
    fun scopedMaven(url: String, vararg paths: String) = maven(url) { content { paths.forEach(::includeGroupAndSubgroups) } }

    scopedMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "me.djtheredstoner")
    scopedMaven("https://repo.hypixel.net/repository/Hypixel", "net.hypixel")
    scopedMaven("https://maven.parchmentmc.org/", "org.parchmentmc")
    scopedMaven("https://api.modrinth.com/maven", "maven.modrinth")
    scopedMaven(
        "https://maven.teamresourceful.com/repository/maven-public/",
        "earth.terrarium",
        "com.teamresourceful",
        "tech.thatgravyboat",
        "me.owdding",
        "com.terraformersmc"
    )
    scopedMaven("https://maven.nucleoid.xyz/", "eu.pb4")
    mavenCentral()
}


fun makeAlias(configuration: String) = if (isUnobfuscated()) configuration else "mod" + configuration.replaceFirstChar { it.uppercase() }

val maybeModImplementation = makeAlias("implementation")
val maybeModCompileOnly = makeAlias("compileOnly")
val maybeModRuntimeOnly = makeAlias("runtimeOnly")
val maybeModApi = makeAlias("api")


val mcVersion = stonecutter.current.version.replace(".", "")
val accessWidenerFile = rootProject.file("src/skyblockpv.obf.accesswidener".replace(if (isUnobfuscated()) ".obf" else "", ""))

val loom = extensions.getByName<LoomGradleExtensionAPI>("loom")
loom.apply {
    log4jConfigs.removeAll { true }
    log4jConfigs.from(rootProject.layout.projectDirectory.file("gradle/log4j.config.xml"))

    runConfigs["client"].apply {
        ideConfigGenerated(true)
        runDir = "../../run"
        vmArg("-Dfabric.modsFolder=" + '"' + rootProject.projectDir.resolve("run/${mcVersion}Mods").absolutePath + '"')
    }

    if (accessWidenerFile.exists()) {
        accessWidenerPath.set(accessWidenerFile)
    }

    mixin {
        defaultRefmapName = "skyblockpv-refmap.json"
    }
}

compactingResources {
    basePath = "repo"
    pathDirectory = "../../src"

    configureTask(tasks.named<AbstractCopyTask>("processResources").get())
}

ksp {
    arg("meowdding.project_name", "SkyBlockPv")
    arg("meowdding.package", "me.owdding.skyblockpv.generated")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(if (isUnobfuscated()) 25 else 21)
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(if (isUnobfuscated()) 25 else 21)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(if (isUnobfuscated()) JvmTarget.JVM_25 else JvmTarget.JVM_21)
    compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
    compilerOptions.freeCompilerArgs.add(
        "-Xnullability-annotations=@org.jspecify.annotations:warn"
    )
}

tasks.processResources {
    val range = if (versionedCatalog.versions.has("minecraft.range")) {
        versionedCatalog.versions.get("minecraft.range").toString()
    } else {
        val start = versionedCatalog.versions.getOrFallback("minecraft.start", "minecraft")
        val end = versionedCatalog.versions.getOrFallback("minecraft.end", "minecraft")
        ">=$start <=$end"
    }
    val replacements = mapOf(
        "version" to version,
        "minecraft_range" to range,
        "fabric_lang_kotlin" to versionedCatalog.versions["fabric.language.kotlin"],
        "rlib" to versionedCatalog.versions["resourceful-lib"],
        "olympus" to versionedCatalog.versions["olympus"],
        "sbapi" to versionedCatalog.versions["skyblockapi"],
        "mlib" to versionedCatalog.versions["meowdding.lib"],
        "rconfigkt" to versionedCatalog.versions["resourceful.configkt"],
        "rconfig" to versionedCatalog.versions["resourceful-config"],
    )
    inputs.properties(replacements)

    filesMatching("fabric.mod.json") {
        expand(replacements)
    }
}

autoMixins {
    mixinPackage = "me.owdding.skyblockpv.mixin"
    projectName = "skyblockpv"
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        excludeDirs.add(file("run"))
    }
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filesMatching(listOf("**/*.fsh", "**/*.vsh")) {
        filter { if (it.startsWith("//!moj_import")) "#${it.substring(3)}" else it }
    }
    with(copySpec {
        from(rootProject.file("src/lang")).include("*.json").into("assets/skyblock-pv/lang")
    })
    with(copySpec {
        from(accessWidenerFile)
        rename { it.replace(".obf", "") }
    })
}

val archiveName = "SkyBlockPv"

base {
    archivesName.set("$archiveName-${archivesName.get()}")
}

tasks.named("build") {
    doLast {
        val sourceFile = rootProject.projectDir.resolve("versions/${project.name}/build/libs/${archiveName}-${stonecutter.current.version}-$version.jar")
        val targetFile = rootProject.projectDir.resolve("build/libs/${archiveName}-$version-${stonecutter.current.version}.jar")
        targetFile.parentFile.mkdirs()
        targetFile.writeBytes(sourceFile.readBytes())
    }
}

tasks.withType<ValidateAccessWidenerTask> { enabled = false }


dependencies {
    "minecraft"(versionedCatalog["minecraft"])

    includeImplementation(versionedCatalog["repolib"])

    "api"(versionedCatalog["skyblockapi"]) {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-${stonecutter.current.version}") }
    }
    "include"(versionedCatalog["skyblockapi"]) {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-${stonecutter.current.version}${"-remapped".takeUnless { isUnobfuscated() } ?: ""}") }
    }
    "api"(versionedCatalog["meowdding.lib"]) {
        capabilities { requireCapability("me.owdding.meowdding-lib:meowdding-lib-${stonecutter.current.version}") }
    }
    "include"(versionedCatalog["meowdding.lib"]) {
        capabilities { requireCapability("me.owdding.meowdding-lib:meowdding-lib-${stonecutter.current.version}${"-remapped".takeUnless { isUnobfuscated() } ?: ""}") }
    }


    includeImplementation(versionedCatalog["placeholders"])
    maybeModImplementation(versionedCatalog["fabric.loader"])
    maybeModImplementation(versionedCatalog["fabric.api"])
    maybeModImplementation(versionedCatalog["mixinconstraints"])
    maybeModRuntimeOnly(versionedCatalog["fabric.language.kotlin"])
    "implementation"(versionedCatalog["fabric.language.kotlin"])
    includeImplementation(versionedCatalog["resourceful.lib"])
    includeImplementation(versionedCatalog["resourceful.config"])
    includeImplementation(versionedCatalog["olympus"])
    includeImplementation(versionedCatalog["resourceful.configkt"])
    "compileOnly"(versionedCatalog["meowdding.ktmodules"])
    "compileOnly"(versionedCatalog["meowdding.ktcodecs"])
    "ksp"(versionedCatalog["meowdding.ktmodules"])
    "ksp"(versionedCatalog["meowdding.ktcodecs"])
}

fun DependencyHandlerScope.includeImplementation(dep: Any) {
    "include"(dep)
    maybeModImplementation(dep)
}
