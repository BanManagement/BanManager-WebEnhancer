pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "BanManagerWebEnhancer"

// Non-Fabric modules (standard includes)
include(":BanManagerWebEnhancerCommon")
include(":BanManagerWebEnhancerBukkit")
include(":BanManagerWebEnhancerBungee")
include(":BanManagerWebEnhancerSponge")
include(":BanManagerWebEnhancerLibs")
include(":BanManagerWebEnhancerVelocity")
include(":BanManagerWebEnhancerE2E")

project(":BanManagerWebEnhancerCommon").projectDir = file("common")
project(":BanManagerWebEnhancerBukkit").projectDir = file("bukkit")
project(":BanManagerWebEnhancerBungee").projectDir = file("bungee")
project(":BanManagerWebEnhancerSponge").projectDir = file("sponge")
project(":BanManagerWebEnhancerLibs").projectDir = file("libs")
project(":BanManagerWebEnhancerVelocity").projectDir = file("velocity")
project(":BanManagerWebEnhancerE2E").projectDir = file("e2e")

// Fabric module with Stonecutter multi-version support
stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    shared {
        // Define version mappings for Fabric
        version("1.20.1", "1.20.1")
        version("1.21.1", "1.21.1")
        version("1.21.4", "1.21.4")
        version("1.21.11", "1.21.11")
    }

    create(":fabric")
}

// Set the fabric project directory
project(":fabric").projectDir = file("fabric")
