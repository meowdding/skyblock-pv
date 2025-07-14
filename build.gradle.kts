@file:Suppress("UnstableApiUsage")

import com.google.devtools.ksp.gradle.KspTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `maven-publish`
    `museum-data` // defined in buildSrc
    alias(libs.plugins.kotlin)
    alias(libs.plugins.terrarium.cloche)
    alias(libs.plugins.meowdding.repo)
    alias(libs.plugins.meowdding.resources)
    alias(libs.plugins.kotlin.symbol.processor)
}

base {
    archivesName.set(project.name.lowercase())
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

cloche {
    metadata {
        modId = "SkyBlockPv"
        name = "SkyBlockPv"
        license = ""
        clientOnly = true

    }

    common {
        mixins.from("src/mixins/meowdding-lib.mixins.json")

        dependencies {
            compileOnly(libs.meowdding.ktcodecs)
            compileOnly(libs.meowdding.ktmodules)

            modImplementation(libs.hypixelapi)
            modImplementation(libs.skyblockapi)
            modImplementation(libs.placeholders) { isTransitive = false }

            modImplementation(libs.fabric.language.kotlin)
        }
    }

    fun createVersion(
        name: String,
        version: String = name,
        loaderVersion: Provider<String> = libs.versions.fabric.loader,
        fabricApiVersion: Provider<String> = libs.versions.fabric.api,
        dependencies: MutableMap<String, Provider<MinimalExternalModuleDependency>>.() -> Unit = { },
    ) {
        val dependencies = mutableMapOf<String, Provider<MinimalExternalModuleDependency>>().apply(dependencies)
        val rlib = dependencies["resourcefullib"]!!
        val rconfig = dependencies["resourcefulconfig"]!!
        val olympus = dependencies["olympus"]!!

        fabric(name) {
            includedClient()
            minecraftVersion = version
            this.loaderVersion = loaderVersion.get()

            include(libs.hypixelapi)
            include(libs.skyblockapi)
            include(rlib)
            include(olympus)
            include(libs.placeholders)

            metadata {
                entrypoint("client") {
                    adapter = "kotlin"
                    value = "me.owdding.lib.MeowddingLib"
                }

                fun dependency(modId: String, version: Provider<String>) {
                    dependency {
                        this.modId = modId
                        this.required = true
                        version {
                            this.start = version
                        }
                    }
                }

                dependency("fabric-language-kotlin", libs.versions.fabric.language.kotlin)
                dependency("resourcefullib", rlib.map { it.version!! })
                dependency("skyblock-api", libs.versions.skyblockapi)
                dependency("olympus", olympus.map { it.version!! })
                dependency("placeholder-api", libs.versions.placeholders)
            }

            dependencies {
                fabricApi(fabricApiVersion, minecraftVersion)
                modImplementation(olympus)
                modImplementation(rconfig)
            }

            runs {
                client()
            }
        }
    }

    createVersion("1.21.5", fabricApiVersion = provider { "0.127.1" }) {
        this["resourcefullib"] = libs.resourceful.lib1215
        this["resourcefulconfig"] = libs.resourceful.config1215
        this["olympus"] = libs.olympus.lib1215
    }
    createVersion("1.21.7") {
        this["resourcefullib"] = libs.resourceful.lib1217
        this["resourcefulconfig"] = libs.resourceful.config1217
        this["olympus"] = libs.olympus.lib1217
    }

    mappings { official() }
}

repositories {
    maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
    maven(url = "https://maven.msrandom.net/repository/cloche")
    maven(url = "https://maven.msrandom.net/repository/root")
    maven(url = "https://repo.hypixel.net/repository/Hypixel/")
    maven(url = "https://api.modrinth.com/maven")
    maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven(url = "https://maven.nucleoid.xyz")
    mavenCentral()
    mavenLocal()
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE


    filesMatching(listOf("**/*.fsh", "**/*.vsh")) {
        filter { if (it.startsWith("//!moj_import")) "#${it.substring(3)}" else it }
    }

    with(copySpec {
        from("src/main/lang").include("*.json").into("assets/skyblock-pv/lang")
    })
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
    this.basePath = "repo"
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
}

tasks.withType<KspTask> {
    outputs.upToDateWhen { false }
}

ksp {
    arg("meowdding.modules.project_name", project.name)
    arg("meowdding.modules.package", "me.owdding.skyblockpv.generated")
    arg("meowdding.codecs.project_name", project.name)
    arg("meowdding.codecs.package", "me.owdding.skyblockpv.generated")
}
