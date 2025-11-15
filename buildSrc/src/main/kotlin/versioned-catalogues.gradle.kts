import gradle.kotlin.dsl.accessors._2f6785c9131286d9d1030ac08c37b2f3.versionCatalogs

versionCatalogs {
    entries[project] = ForwardingVersionCatalog(
        named("libs${project.name.replace(".", "")}"),
        named("libs")
    )
}
