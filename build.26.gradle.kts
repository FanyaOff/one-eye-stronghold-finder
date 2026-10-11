plugins {
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
    `maven-publish`
}

val mcVersion = project.name
val fabricApiVersion = when (mcVersion) {
    "26.1" -> "0.155.3+26.1.2"
    "26.2" -> "0.161.0+26.2"
    "26.3" -> "0.162.0+26.3"
    else -> error("Unsupported Minecraft version: $mcVersion")
}
val minecraftRange = when (mcVersion) {
    "26.1" -> ">=26.1 <26.2"
    "26.2" -> ">=26.2 <26.3"
    "26.3" -> ">=26.3 <26.4"
    else -> error("Unsupported Minecraft version: $mcVersion")
}

version = "${property("mod_version")}+$mcVersion"
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

loom {
    splitEnvironmentSourceSets()
    mods {
        create("one_eye_stronghold_finder") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version_range", minecraftRange)
    inputs.property("java_version", 25)
    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "version" to project.version,
            "minecraft_version_range" to minecraftRange,
            "java_version" to 25
        ))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${property("archives_base_name")}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("archives_base_name") as String
            from(components["java"])
        }
    }
}
