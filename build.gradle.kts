import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("com.modrinth.minotaur") version "2.+"
    id("org.gradlex.reproducible-builds") version "1.1"
}

val mod_version: String by project
val maven_group: String by project
val archives_base_name: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val fabric_kotlin_version: String by project
val patchouli_version: String by project
val paucal_version: String by project
val hexcasting_version: String by project
val cardinal_components_version: String by project
val cloth_config_version: String by project
val inline_version: String by project

version = mod_version
group = maven_group

base {
    archivesName = archives_base_name
}

repositories {
   // hexcasting
    maven { url = uri("https://maven.blamejared.com/") }
    // mod menu
    maven { url = uri("https://maven.terraformersmc.com/releases") }
    // cloth config
    maven { url = uri("https://maven.shedaniel.me/") }
    // cardinal components
    maven { url = uri("https://maven.ladysnake.org/releases") }
}

loom {
    splitEnvironmentSourceSets()
    mods {
        create("hexchanting") {
            sourceSet(sourceSets.getByName("main"))
            sourceSet(sourceSets.getByName("client"))
        }
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("net.minecraft:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loader_version")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")

    modImplementation("vazkii.patchouli:Patchouli:$minecraft_version-$patchouli_version-FABRIC")
    modImplementation("at.petra-k.paucal:paucal-fabric-$minecraft_version:$paucal_version")
    modImplementation("at.petra-k.hexcasting:hexcasting-fabric-$minecraft_version:$hexcasting_version") {
        exclude(module = "phosphor")
        exclude(module = "lithium")
        exclude(module = "emi")
    }

    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-api:$cardinal_components_version")
    modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:$cloth_config_version")
    modLocalRuntime("com.samsthenerd.inline:inline-fabric:$minecraft_version-$inline_version")
    // We crash on launch without this. I have no idea why, I'm just stealing Hexcellular's solution
    modLocalRuntime(files("${rootProject.rootDir}/libs/serialization-hooks-0.4.99999.jar"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to inputs.properties["version"])
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
    inputs.property("archivesName", base.archivesName.get())

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = archives_base_name
            from(components["java"])
        }
    }

    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}

modrinth {
    // Remember to have the MODRINTH_TOKEN environment variable set or else this will fail
    // Just make sure it stays private!
    token.set(System.getenv("MODRINTH_TOKEN"))
    // This can be the project ID or the slug. Either will work!
    projectId.set("hexchanting")
    versionType.set("release") // This is the default -- can also be `beta` or `alpha`
    uploadFile.set(tasks.remapJar) // With Loom, this MUST be set to `remapJar` instead of `jar`!
    additionalFiles.add(tasks.remapSourcesJar)
    gameVersions.add("1.20.1") // Must be an array, even with only one version
    loaders.add("fabric") // Must also be an array - no need to specify this if you're using Loom or ForgeGradle
    dependencies { // A special DSL for creating dependencies
        // scope.type
        // The scope can be `required`, `optional`, `incompatible`, or `embedded`
        // The type can either be `project` or `version`
        required.project("hex-casting")
    }
}
