enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "SkyBlockPV"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
        maven(url = "https://maven.msrandom.net/repository/cloche")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}
