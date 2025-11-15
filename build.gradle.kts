@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    id("fabric-loom")
    `versioned-catalogues`
    kotlin("jvm") version "2.2.20"
    alias(libs.plugins.meowdding.resources)
    alias(libs.plugins.meowdding.auto.mixins)
    alias(libs.plugins.kotlin.symbol.processor)
}

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

configurations {
    modImplementation {
        attributes.attribute(Attribute.of("earth.terrarium.cloche.modLoader", String::class.java), "fabric")
    }
}

dependencies {
    attributesSchema {
        attribute(Attribute.of("earth.terrarium.cloche.minecraftVersion", String::class.java)) {
            disambiguationRules.add(ClocheDisambiguationRule::class) {
                params(versionedCatalog.versions.getOrFallback("sbapi-mc-version", "minecraft").toString())
            }
        }
    }

    minecraft(versionedCatalog["minecraft"])
    mappings(loom.layered {
        officialMojangMappings()
        parchment(variantOf(versionedCatalog["parchment"]) {
            artifactType("zip")
        })
    })
    includeImplementation(libs.skyblockapi)
    includeImplementation(libs.meowdding.lib)
    //includeImplementation(libs.meowdding.remote.repo)
    //includeImplementation(versionedCatalog["placeholders"])
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.language.kotlin)
    modImplementation(versionedCatalog["fabric.api"])
    includeImplementation(versionedCatalog["resourceful.lib"])
    includeImplementation(versionedCatalog["resourceful.config"])
    includeImplementation(versionedCatalog["olympus"])
    includeImplementation(libs.keval)
    includeImplementation(libs.resourceful.config.kotlin)
    compileOnly(libs.meowdding.ktmodules)
    compileOnly(libs.meowdding.ktcodecs)
    ksp(libs.meowdding.ktmodules)
    ksp(libs.meowdding.ktcodecs)
}

fun DependencyHandler.includeImplementation(dep: Any) {
    include(dep)
    modImplementation(dep)
}

val mcVersion = stonecutter.current.version.replace(".", "")
loom {
    runConfigs["client"].apply {
        ideConfigGenerated(true)
        runDir = "../../run"
        vmArg("-Dfabric.modsFolder=" + '"' + rootProject.projectDir.resolve("run/${mcVersion}Mods").absolutePath + '"')
    }

    val accessWidenerFile = project.file("skyblockpv.accesswidener")
    if (accessWidenerFile.exists()) {
        accessWidenerPath.set(accessWidenerFile)
    }
}

compactingResources {
    basePath = "repo"
    pathDirectory = "../../src"

    configureTask(tasks.named<AbstractCopyTask>("processResources").get())

    compactToObject("garden_data")
    compactToObject("chocolate_factory")
    compactToObject("rift")
    compactToArray("museum_categories")
    substituteFromDifferentFile("slayer", "slayers")
    compactToObject("pets/overwrites")
    compactToObject("pets")
    compactToObject("crimson_isle/dojo")
    compactToObject("crimson_isle/kuudra")
    compactToObject("crimson_isle")
    compactToArray("minions/categories")
    compactToObject("minions")
    downloadResource("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/constants/bestiary.json", "bestiary.json")
    downloadResource("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/refs/heads/master/constants/misc.json", "neu_misc.json")
}

ksp {
    arg("meowdding.project_name", "SkyBlockPv")
    arg("meowdding.package", "me.owdding.skyblockpv.generated")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
}

tasks.processResources {
    val replacements = mapOf(
        "version" to version,
        "minecraft_start" to versionedCatalog.versions.getOrFallback("minecraft.start", "minecraft"),
        "minecraft_end" to versionedCatalog.versions.getOrFallback("minecraft.end", "minecraft"),
        "fabric_lang_kotlin" to libs.versions.fabric.language.kotlin.get(),
        "rlib" to versionedCatalog.versions["resourceful-lib"],
        "olympus" to versionedCatalog.versions["olympus"],
        "sbapi" to libs.versions.skyblockapi.get(),
        "mlib" to libs.versions.meowdding.lib.get(),
        "rconfigkt" to libs.versions.resourceful.config.kotlin.get(),
        "rconfig" to versionedCatalog.versions["resourceful.config"],
    )
    inputs.properties(replacements)

    filesMatching("fabric.mod.json") {
        expand(replacements)
    }
}

autoMixins {
    mixinPackage = "me.owdding.skyblockpv.mixins"
    projectName = "skyblock-pv"
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
