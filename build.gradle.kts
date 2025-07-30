@file:Suppress("UnstableApiUsage")

import com.google.devtools.ksp.gradle.KspTask
import earth.terrarium.cloche.api.metadata.ModMetadata
import net.msrandom.minecraftcodev.core.utils.toPath
import net.msrandom.minecraftcodev.fabric.task.JarInJar
import net.msrandom.minecraftcodev.runs.task.WriteClasspathFile
import net.msrandom.stubs.GenerateStubApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.path.readText
import kotlin.io.path.writeText

plugins {
    idea
    `maven-publish`
    `museum-data` // defined in buildSrc
    alias(libs.plugins.kotlin)
    alias(libs.plugins.terrarium.cloche)
    alias(libs.plugins.terrarium.multiplatform)
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

dependencies {
    ksp(libs.meowdding.ktcodecs)
    ksp(libs.meowdding.ktmodules)
    compileOnly(libs.keval)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_0
        freeCompilerArgs.addAll(
            "-Xmulti-platform",
            "-Xno-check-actual",
            "-Xexpect-actual-classes",
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
            accessWideners.from("src/$name/skyblock-pv.${sourceSet.name}.accesswidener")

            //include(libs.hypixelapi) included in sbapi
            include(libs.skyblockapi)
            include(libs.meowdding.lib)
            include(rlib)
            include(rconfigkt)
            include(rconfig)
            include(olympus)
            include(libs.placeholders)
            include(libs.keval)
            include(libs.mixinconstraints)
            include(libs.repolib)
            mixins.from("src/mixins/skyblock-pv.${sourceSet.name}.mixins.json")

            metadata {
                entrypoint("client") {
                    adapter = "kotlin"
                    value = "me.owdding.skyblockpv.SkyBlockPv"
                }

                fun dependency(modId: String, version: Provider<String>? = null) {
                    dependency {
                        this.modId = modId
                        this.required = true
                        if (version != null) version {
                            this.start = version
                        }
                    }
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
                dependency("resourcefulconfig", rconfig.map { it.version!! })
                dependency("meowdding-lib", libs.versions.meowdding.lib)
            }

            dependencies {
                fabricApi(fabricApiVersion, minecraftVersion)
                modImplementation(olympus)
                modImplementation(rconfig)
                modImplementation(rconfigkt)
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

    mappings { official() }
}

tasks.named("createCommonApiStub", GenerateStubApi::class).configure {
    excludes.add(libs.skyblockapi.get().module.toString())
    excludes.add(libs.meowdding.lib.get().module.toString())
}

repositories {
    maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
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

    filesMatching(listOf("**/*.fsh", "**/*.vsh")) {
        filter { if (it.startsWith("//!moj_import")) "#${it.substring(3)}" else it }
    }

    with(copySpec {
        from("src/lang").include("*.json").into("assets/skyblock-pv/lang")
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
    basePath = "repo"
    configureTask(tasks.getByName<ProcessResources>("process1218Resources"))
    configureTask(tasks.getByName<ProcessResources>("process1215Resources"))
    configureTask(tasks.getByName<ProcessResources>("processResources"))

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

afterEvaluate {
    tasks.withType<KspTask>().configureEach {
        outputs.upToDateWhen { false }
    }
}

sourceSets {
    getByName("main") {
        resources.srcDir(project.layout.projectDirectory.dir("src/resources"))
    }
}

ksp {
    this@ksp.excludedSources.from(sourceSets.getByName("1215").kotlin.srcDirs)
    this@ksp.excludedSources.from(sourceSets.getByName("1218").kotlin.srcDirs)
    arg("meowdding.modules.project_name", project.name)
    arg("meowdding.modules.package", "me.owdding.skyblockpv.generated")
    arg("meowdding.codecs.project_name", project.name)
    arg("meowdding.codecs.package", "me.owdding.skyblockpv.generated")
}

// TODO temporary workaround for a cloche issue on certain systems, remove once fixed
tasks.withType<WriteClasspathFile>().configureEach {
    actions.clear()
    actions.add {
        generate()
        val file = output.get().toPath()
        file.writeText(file.readText().lines().joinToString(File.pathSeparator))
    }
}

val mcVersions = sourceSets.filterNot { it.name == SourceSet.MAIN_SOURCE_SET_NAME || it.name == SourceSet.TEST_SOURCE_SET_NAME }.map { it.name }

tasks.register("release") {
    group = "meowdding"
    mcVersions.forEach {
        tasks.findByName("${it}JarInJar")?.let { task ->
            dependsOn(task)
            mustRunAfter(task)
        }
    }
}

tasks.register("cleanRelease") {
    group = "meowdding"
    listOf("clean", "release").forEach {
        tasks.getByName(it).let { task ->
            dependsOn(task)
            mustRunAfter(task)
        }
    }
}

tasks.withType<JarInJar>().configureEach {
    include { !it.name.endsWith("-dev.jar") }

    manifest {
        attributes["Fabric-Loom-Mixin-Remap-Type"] = "static"
        attributes["Fabric-Jar-Type"] = "classes"
        attributes["Fabric-Mapping-Namespace"] = "intermediary"
    }
}

tasks.register("setupForWorkflows") {
    mcVersions.flatMap {
        listOf("remap${it}CommonMinecraftNamed", "remap${it}ClientMinecraftNamed")
    }.mapNotNull { tasks.findByName(it) }.forEach {
        dependsOn(it)
        mustRunAfter(it)
    }
}
