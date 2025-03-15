import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.incremental.createDirectory
import kotlin.io.path.readText

plugins {
    idea
    kotlin("jvm") version "2.0.20"
    alias(libs.plugins.loom)
    id("maven-publish")
}

base {
    archivesName.set(project.name.lowercase())
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

loom {
    runs {
        getByName("client") {
            programArg("--quickPlayMultiplayer=hypixel.net")
            vmArg("-Ddevauth.enabled=true")
            vmArg("-Dskyblockapi.debug=true")
        }
    }

    // Mixin Hotswap might break more than it fixes
    /*afterEvaluate {
        val mixinPath = configurations.compileClasspath.get()
            .files { it.group == "net.fabricmc" && it.name == "sponge-mixin" }
            .first()
        runConfigs {
            "client" {
                vmArgs.add("-javaagent:$mixinPath")
            }
        }
    }*/
}

repositories {
    maven(url = "https://nexus.resourcefulbees.com/repository/maven-public/")
    maven(url = "https://repo.hypixel.net/repository/Hypixel/")
    maven(url = "https://api.modrinth.com/maven")
    maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven(url = "https://maven.azureaaron.net/releases")
    maven(url = "https://maven.nucleoid.xyz")
    mavenCentral()
    mavenLocal()
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.3:2024.12.07@zip")
    })
    modImplementation(libs.loader)
    modImplementation(libs.fabrickotlin)
    modImplementation(libs.fabric)

    modImplementation(libs.hypixelapi)
    modImplementation(libs.skyblockapi)
    modImplementation(libs.rconfig)
    modImplementation(libs.rconfigkt)
    modImplementation(libs.rlib)
    modImplementation(libs.olympus)
    modImplementation(libs.legacydfu)
    modImplementation(libs.mixinconstraints)
    modImplementation(libs.placeholders)
    implementation(libs.keval)

    include(libs.hypixelapi)
    include(libs.skyblockapi)
    include(libs.rconfig)
    include(libs.rconfigkt)
    include(libs.rlib)
    include(libs.olympus)
    include(libs.legacydfu)
    include(libs.mixinconstraints)
    include(libs.placeholders)
    include(libs.keval)

    modRuntimeOnly(libs.devauth)
    modRuntimeOnly(libs.modmenu)
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    filesMatching(listOf("fabric.mod.json")) {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
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

tasks.withType<ProcessResources>().configureEach {
    exclude("repo/hotmperks/**")

    val jsonArray = JsonArray()

    val idsUsed: MutableSet<String> = mutableSetOf()
    fun validate(jsonElement: JsonElement, file: File) = jsonElement.also {
        if (it !is JsonObject) {
            throw UnsupportedOperationException("${file.name} must only contain a json object!")
        }

        if (idsUsed.contains(it.get("id").asString)) {
            throw UnsupportedOperationException("Duplicate id found in ${file.name}!")
        }
        idsUsed.add(it.get("id").asString)
    }

    sourceSets.main.get().resources.srcDirs.map { it.toPath() }.forEach {
        fileTree(it) {
            include("repo/hotmperks/*.json")
            forEach {
                jsonArray.add(validate(JsonParser.parseString(it.toPath().readText()), it))
            }
        }
    }

    val content = jsonArray.toString()
    val file = project.layout.buildDirectory.file("tmp/generated_hotm/repo/hotm.json").get()
    file.asFile.parentFile.createDirectory()
    file.asFile.writeText(content)
    from(project.layout.buildDirectory.dir("tmp/generated_hotm/").get())
}
