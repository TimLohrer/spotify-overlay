pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://repo.spongepowered.org/maven")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.neoforged.net/releases/")
        maven("https://modmaven.dev/")
    }
    plugins {
        kotlin("jvm") version "2.3.0"
        id("me.modmuss50.mod-publish-plugin") version "0.8.0"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        version("1.21-fabric","1.21")
        version("1.21.1-fabric","1.21.1")
        version("1.21.4-fabric","1.21.4")
        version("1.21.5-fabric","1.21.5")
        version("1.21.7-fabric","1.21.7")
        version("1.21.8-fabric","1.21.8")
        version("1.21.10-fabric","1.21.10")
        version("1.21.11-fabric","1.21.11")
        vcsVersion="1.21.11-fabric"
    }
    create(rootProject)
}

rootProject.name = "spotify-overlay"