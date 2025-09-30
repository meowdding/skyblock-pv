@file:Suppress("UnstableApiUsage")

import com.google.devtools.ksp.gradle.KspTask
import earth.terrarium.cloche.api.metadata.ModMetadata
import me.owdding.gradle.dependency
import net.msrandom.minecraftcodev.core.utils.toPath
import net.msrandom.minecraftcodev.fabric.task.JarInJar
import net.msrandom.stubs.GenerateStubApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.path.exists

plugins {
    idea
    `maven-publish`
    `museum-data` // defined in buildSrc
    alias(libs.plugins.kotlin)
    alias(libs.plugins.terrarium.cloche)
    alias(libs.plugins.terrarium.multiplatform)
    alias(libs.plugins.meowdding.repo)
    alias(libs.plugins.meowdding.resources)
    alias(libs.plugins.meowdding.gradle)
    alias(libs.plugins.kotlin.symbol.processor)
}

base {
    archivesName.set(project.name.lowercase())
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

dependencies {
    ksp(libs.meowdding.ktcodecs)
    ksp(libs.meowdding.ktmodules)
    compileOnly(libs.keval)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_2
        freeCompilerArgs.addAll(
            "-Xmulti-platform",
            "-Xno-check-actual",
            "-Xexpect-actual-classes",
            "-Xopt-in=kotlin.time.ExperimentalTime",
        )
    }
}

cloche {
    metadata {
        modId = "skyblockpv"
        name = "SkyBlockPv"
        license = ""
        clientOnly = true
        icon = "assets/skyblock-pv/skyblock-pv.png"
    }

    common {
        mixins.from("src/mixins/skyblock-pv.common.mixins.json")
        accessWideners.from("src/skyblock-pv.accesswidener")

        dependencies {
            compileOnly(libs.meowdding.ktcodecs)
            compileOnly(libs.meowdding.ktmodules)
            implementation(libs.keval)
            implementation(libs.repolib)

            modImplementation(libs.hypixelapi)
            modImplementation(libs.mixinconstraints)
            modImplementation(libs.skyblockapi)
            modImplementation(libs.meowdding.lib)
            modImplementation(libs.placeholders) { isTransitive = false }

            modImplementation(libs.fabric.language.kotlin)
        }
    }

    fun createVersion(
        name: String,
        version: String = name,
        loaderVersion: Provider<String> = libs.versions.fabric.loader,
        fabricApiVersion: Provider<String> = libs.versions.fabric.api,
        minecraftVersionRange: ModMetadata.VersionRange.() -> Unit = {
            start = version
            end = version
            endExclusive = false
        },
        dependencies: MutableMap<String, Provider<MinimalExternalModuleDependency>>.() -> Unit = { },
    ) {
        val dependencies = mutableMapOf<String, Provider<MinimalExternalModuleDependency>>().apply(dependencies)
        val rlib = dependencies["resourcefullib"]!!
        val rconfig = dependencies["resourcefulconfig"]!!
        val rconfigkt = dependencies["resourcefulconfigkt"]!!
        val olympus = dependencies["olympus"]!!


        fabric(name) {
            includedClient()
            minecraftVersion = version
            this.loaderVersion = loaderVersion.get()
            val accessWidenerFile = layout.projectDirectory.file("src/$name/skyblock-pv.${sourceSet.name}.accesswidener")
            val mixinFile = layout.projectDirectory.file("src/mixins/skyblock-pv.${sourceSet.name}.mixins.json")

            if (accessWidenerFile.toPath().exists()) {
                accessWideners.from(accessWidenerFile)
            }

            if (mixinFile.toPath().exists()) {
                mixins.from(mixinFile)
            }

            metadata {
                entrypoint("client") {
                    adapter = "kotlin"
                    value = "me.owdding.skyblockpv.SkyBlockPv"
                }

                dependency {
                    modId = "minecraft"
                    required = true
                    version(minecraftVersionRange)
                }
                dependency("fabric")
                dependency("fabricloader", loaderVersion)
                dependency("fabric-language-kotlin", libs.versions.fabric.language.kotlin)
                dependency("resourcefullib", rlib.map { it.version!! })
                dependency("skyblock-api", libs.versions.skyblockapi)
                dependency("olympus", olympus.map { it.version!! })
                dependency("placeholder-api", libs.versions.placeholders)
                dependency("resourcefulconfigkt", rconfigkt.map { it.version!! })
                //dependency("resourcefulconfig", rconfig.map { it.version!! })
                dependency("meowdding-lib", libs.versions.meowdding.lib)
            }

            dependencies {
                fabricApi(fabricApiVersion, minecraftVersion)
                modImplementation(olympus)
                modImplementation(rconfig)
                modImplementation(rconfigkt)


                include(libs.skyblockapi)
                include(libs.meowdding.lib)
                include(rlib)
                include(rconfigkt)
                include(rconfig)
                include(olympus)
                include(libs.placeholders)
                include(libs.keval)
                include(libs.mixinconstraints)
            }

            runs {
                client {
                    arguments("--quickPlaySingleplayer=\"${name.replace(".", "")}\"")
                    jvmArgs("-Ddevauth.enabled=true")
                }
            }
        }
    }

    createVersion("1.21.5", fabricApiVersion = provider { "0.127.1" }) {
        this["resourcefullib"] = libs.resourceful.lib1215
        this["resourcefulconfig"] = libs.resourceful.config1215
        this["resourcefulconfigkt"] = libs.resourceful.configkt1215
        this["olympus"] = libs.olympus.lib1215
    }
    createVersion("1.21.8", minecraftVersionRange = {
        start = "1.21.6"
    }) {
        this["resourcefullib"] = libs.resourceful.lib1218
        this["resourcefulconfig"] = libs.resourceful.config1218
        this["resourcefulconfigkt"] = libs.resourceful.configkt1218
        this["olympus"] = libs.olympus.lib1218
    }
    createVersion("1.21.9", fabricApiVersion = provider { "0.133.7" }) {
        this["resourcefullib"] = libs.resourceful.lib1219
        this["resourcefulconfig"] = libs.resourceful.config1219
        this["resourcefulconfigkt"] = libs.resourceful.configkt1219
        this["olympus"] = libs.olympus.lib1219
    }

    mappings { official() }
}

tasks.named("createCommonApiStub", GenerateStubApi::class).configure {
    excludes.addAll(
        "org.jetbrains.kotlin",
        "me.owdding",
        "net.hypixel",
        "maven.modrinth",
        "com.fasterxml.jackson",
        "com.google",
        "com.ibm",
        "io.netty",
        "net.fabricmc:fabric-language-kotlin",
        "com.mojang:datafixerupper",
        "com.mojang:brigardier",
        "io.github.llamalad7:mixinextras",
        "net.minidev",
        "com.nimbusds",
        "tech.thatgravyboat",
        "net.msrandom",
        "eu.pb4"
    )
}

repositories {
    maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://maven.teamresourceful.com/repository/msrandom/")
    maven(url = "https://repo.hypixel.net/repository/Hypixel/")
    maven(url = "https://api.modrinth.com/maven")
    maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven(url = "https://maven.nucleoid.xyz")
    mavenCentral()
    mavenLocal()
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        excludeDirs.add(file("run"))
    }
}

repo {
    hotm { includeAll() }
    sacks { includeAll() }
}

compactingResources {
    basePath = "repo"

    tasks.withType<ProcessResources> { configureTask(this) }

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

afterEvaluate {
    tasks.withType<KspTask>().configureEach {
        outputs.upToDateWhen { false }
    }
}

tasks.withType<JarInJar>().configureEach {
    archiveBaseName = "SkyBlockPv"
}

meowdding {
    generatedPackage = "me.owdding.skyblockpv.generated"

    setupClocheClasspathFix()
    hasAccessWideners = true
    configureCodecs = true
    configureModules = true
}
