import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.dependencies.DefaultMinimalDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `maven-publish`
    `museum-data` // defined in buildSrc
    alias(libs.plugins.kotlin)
    alias(libs.plugins.loom)
    alias(libs.plugins.repo)
    alias(libs.plugins.resources)
    alias(libs.plugins.ksp)
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
    maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
    maven(url = "https://repo.hypixel.net/repository/Hypixel/")
    maven(url = "https://api.modrinth.com/maven")
    maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven(url = "https://maven.nucleoid.xyz")
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(libs.meowdding.ktmodules)
    ksp(libs.meowdding.ktmodules)

    minecraft(libs.minecraft)
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment(libs.parchmentmc.get().withMcVersion().toString())
    })

    modImplementation(libs.bundles.fabric)

    implementation(libs.repo) // included in sbapi, exposed through implementation

    includeModImplementationBundle(libs.bundles.sbapi)
    includeModImplementationBundle(libs.bundles.rconfig)
    includeModImplementationBundle(libs.bundles.libs)
    includeModImplementationBundle(libs.bundles.meowdding)

    includeModImplementation(libs.mixinconstraints)

    includeImplementation(libs.keval)

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

ksp {
    arg("meowdding.modules.project_name", project.name)
    arg("meowdding.modules.package", "me.owdding.skyblockpv.generated")
}

fun ExternalModuleDependency.withMcVersion(): ExternalModuleDependency {
    return DefaultMinimalDependency(
        DefaultModuleIdentifier.newId(this.group, this.name.replace("<mc_version>", libs.versions.minecraft.get())),
        DefaultMutableVersionConstraint(this.versionConstraint)
    )
}

@Suppress("unused")
fun DependencyHandlerScope.includeImplementationBundle(bundle: Provider<ExternalModuleDependencyBundle>) = bundle.get().forEach {
    includeImplementation(provider { it })
}

fun DependencyHandlerScope.includeModImplementationBundle(bundle: Provider<ExternalModuleDependencyBundle>) = bundle.get().forEach {
    includeModImplementation(provider { it })
}

fun <T : ExternalModuleDependency> DependencyHandlerScope.includeImplementation(dependencyNotation: Provider<T>) =
    with(dependencyNotation.get().withMcVersion()) {
        include(this)
        modImplementation(this)
    }

fun <T : ExternalModuleDependency> DependencyHandlerScope.includeModImplementation(dependencyNotation: Provider<T>) =
    with(dependencyNotation.get().withMcVersion()) {
        include(this)
        modImplementation(this)
    }
