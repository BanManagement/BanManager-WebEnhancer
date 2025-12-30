import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `java-library`
    `maven-publish`
    `fabric-loom`
}

// Read version-specific properties
val minecraftVersion: String by project.extra { property("minecraft_version") as String }
val yarnMappings: String by project.extra { property("yarn_mappings") as String }
val fabricLoaderVersion: String by project.extra { property("fabric_loader") as String }
val fabricApiVersion: String by project.extra { property("fabric_api") as String }
val javaVersion: String by project.extra { property("java_version") as String }

// Stonecutter version check helper
val mcVersion = minecraftVersion.split(".").let { parts ->
    val major = parts.getOrElse(0) { "1" }.toIntOrNull() ?: 1
    val minor = parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0
    val patch = parts.getOrElse(2) { "0" }.toIntOrNull() ?: 0
    Triple(major, minor, patch)
}
val isPreV21 = mcVersion.second < 21

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

// Configure Java toolchain based on MC version
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    // Base modules available in all versions
    val baseModules = listOf(
        "fabric-api-base",
        "fabric-lifecycle-events-v1"
    )

    baseModules.forEach {
        modImplementation(fabricApi.module(it, fabricApiVersion))
    }

    // Command API: v1 for 1.20.1, v2 for 1.21+
    if (isPreV21) {
        modImplementation(fabricApi.module("fabric-command-api-v1", fabricApiVersion))
    } else {
        modImplementation(fabricApi.module("fabric-command-api-v2", fabricApiVersion))
    }

    // Fabric Permissions API (provided by BanManager Fabric at runtime)
    modImplementation("me.lucko:fabric-permissions-api:0.3.1")

    // WebEnhancer common
    api(project(":BanManagerWebEnhancerCommon")) {
        isTransitive = true
    }

    // BanManager Fabric (compileOnly - provided at runtime)
    modCompileOnly("me.confuser.banmanager:BanManagerFabric-mc$minecraftVersion:7.10.0-SNAPSHOT")
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    val internalVersion = project.ext["internalVersion"]
    val commandApiModule = if (isPreV21) "fabric-command-api-v1" else "fabric-command-api-v2"

    inputs.property("internalVersion", internalVersion)
    inputs.property("minecraftVersion", minecraftVersion)
    inputs.property("commandApiModule", commandApiModule)

    filesMatching(listOf("plugin.yml", "fabric.mod.json")) {
        expand(
            "internalVersion" to internalVersion,
            "mainPath" to "me.confuser.banmanager.webenhancer.fabric.FabricPlugin",
            "minecraftVersion" to minecraftVersion,
            "commandApiModule" to commandApiModule
        )
    }
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    archiveBaseName.set("BanManagerWebEnhancerFabric")
    archiveClassifier.set("mc$minecraftVersion")
    archiveVersion.set("")

    dependencies {
        include(dependency(":BanManagerWebEnhancerCommon"))
        include(dependency(":BanManagerWebEnhancerLibs"))
    }
    exclude("GradleStart**")
    exclude(".cache");
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
    exclude("/mappings/*")
    exclude("org/apache/logging/**")
    exclude("Log4j-**")
    exclude("META-INF/org/apache/logging/**")

    minimize()
}

tasks.named<RemapJarTask>("remapJar") {
    dependsOn(tasks.named<ShadowJar>("shadowJar"))

    inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
    archiveBaseName.set("BanManagerWebEnhancerFabric")
    archiveClassifier.set("mc$minecraftVersion")
    archiveVersion.set("")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
