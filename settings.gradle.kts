pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.11"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "BanManagerWebEnhancer"

// Non-Fabric modules (standard includes)
include(":BanManagerWebEnhancerCommon")
include(":BanManagerWebEnhancerBukkit")
include(":BanManagerWebEnhancerBungee")
include(":BanManagerWebEnhancerSponge")
include(":BanManagerWebEnhancerSponge7")
include(":BanManagerWebEnhancerLibs")
include(":BanManagerWebEnhancerVelocity")
include(":BanManagerWebEnhancerE2E")

project(":BanManagerWebEnhancerCommon").projectDir = file("common")
project(":BanManagerWebEnhancerBukkit").projectDir = file("bukkit")
project(":BanManagerWebEnhancerBungee").projectDir = file("bungee")
project(":BanManagerWebEnhancerSponge").projectDir = file("sponge")
project(":BanManagerWebEnhancerSponge7").projectDir = file("sponge-api7")
project(":BanManagerWebEnhancerLibs").projectDir = file("libs")
project(":BanManagerWebEnhancerVelocity").projectDir = file("velocity")
project(":BanManagerWebEnhancerE2E").projectDir = file("e2e")

// Fabric module with Stonecutter multi-version support
stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    shared {
        // Define version mappings for Fabric
        vers("1.20.1", "1.20.1")
        vers("1.21.1", "1.21.1")
        vers("1.21.4", "1.21.4")
    }

    create(":fabric")
}

// Set the fabric project directory
project(":fabric").projectDir = file("fabric")
