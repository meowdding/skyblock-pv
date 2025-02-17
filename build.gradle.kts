import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    kotlin("jvm") version "2.0.0"
    alias(libs.plugins.loom)
}

repositories {
    maven("https://maven.teamresourceful.com/repository/maven-public/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21")
    mappings(loom.officialMojangMappings())

    modImplementation(libs.fabricLoader)
    modImplementation(libs.fabricApi)
    modImplementation(libs.fabricKt)

    modImplementation(libs.devauth)

    modImplementation(libs.resourcefulconfig)
    modImplementation(libs.resourcefulconfigkt)
    include(libs.resourcefulconfigkt)
}

loom {
    runs {
        getByName("client") {
            property("devauth.configDir", rootProject.file(".devauth").absolutePath)
        }
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

}

java {
    withSourcesJar()
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}