import dev.kikugie.stonecutter.settings.StonecutterSettings

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://repo.spongepowered.org/maven")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
    }
    plugins {
        kotlin("jvm") version "2.2.0"
        id("dev.architectury.loom") version "1.10-SNAPSHOT"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.6-beta.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
extensions.configure<StonecutterSettings> {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        vers("1.21-fabric","1.21")
        vers("1.21.1-fabric","1.21.1")
        vers("1.21.4-fabric","1.21.4")
        vers("1.21.5-fabric","1.21.5")
        vers("1.21.7-fabric","1.21.7")
        vers("1.21.8-fabric","1.21.8")
        vcsVersion="1.21.5-fabric"
    }
    create(rootProject)
}

rootProject.name = "spotify-overlay"