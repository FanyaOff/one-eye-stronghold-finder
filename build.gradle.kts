plugins {
    id("fabric-loom") version "1.17-SNAPSHOT"
    `maven-publish`
}

val mcVersion = project.name
val legacyLine = mcVersion.startsWith("1.")
val javaVersion = if (legacyLine) 21 else 25
val fabricApiVersion = when (mcVersion) {
    "1.21.8" -> "0.136.1+1.21.8"
    "1.21.9" -> "0.134.1+1.21.9"
    "1.21.10" -> "0.138.4+1.21.10"
    "1.21.11" -> "0.141.6+1.21.11"
    else -> error("Unsupported Minecraft version: $mcVersion")
}
val minecraftRange = ">=$mcVersion <${when (mcVersion) {
    "1.21.8" -> "1.21.9"
    "1.21.9" -> "1.21.10"
    "1.21.10" -> "1.21.11"
    "1.21.11" -> "1.21.12"
    else -> error("Unsupported Minecraft version: $mcVersion")
}}"

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
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version_range", minecraftRange)
    inputs.property("java_version", javaVersion)
    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "version" to project.version,
            "minecraft_version_range" to minecraftRange,
            "java_version" to javaVersion
        ))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
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
