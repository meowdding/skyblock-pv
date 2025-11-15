import gradle.kotlin.dsl.accessors._e422e1462cee4e63d7e2347987997eb8.versionCatalogs

versionCatalogs {
    entries[project] = ForwardingVersionCatalog(
        named("libs${project.name.replace(".", "")}"),
        named("libs")
    )
}
