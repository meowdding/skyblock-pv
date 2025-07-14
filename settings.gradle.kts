enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "SkyBlockPV"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://maven.msrandom.net/repository/cloche")
        maven("https://maven.teamresourceful.com/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}
