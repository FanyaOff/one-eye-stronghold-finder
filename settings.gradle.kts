pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    centralScript = "build.gradle.kts"
    create(rootProject) {
        version("1.21.8")
        version("1.21.9")
        version("1.21.10")
        version("1.21.11")
        version("26.1").buildscript = "build.26.gradle.kts"
        version("26.2").buildscript = "build.26.gradle.kts"
        version("26.3").buildscript = "build.26.gradle.kts"
    }
}

rootProject.name = "one-eye-stronghold-finder"
