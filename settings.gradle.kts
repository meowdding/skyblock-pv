enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "SkyBlockPV"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
        maven("https://maven.teamresourceful.com/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}
