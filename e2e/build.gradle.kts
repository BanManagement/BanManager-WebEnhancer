plugins {
    base
}

description = "E2E tests for BanManager-WebEnhancer using Docker and Mineflayer"

// BanManager path - defaults to sibling directory for local dev, CI sets via property
val banManagerPath: String = findProperty("banManagerPath")?.toString()
    ?: System.getenv("BANMANAGER_PATH")
    ?: "../BanManager"

// Resolve relative to root project for local dev, or as-is for CI (which passes absolute or relative-to-root)
val banManagerDir = if (banManagerPath.startsWith("/")) {
    file(banManagerPath)
} else {
    rootProject.file(banManagerPath)
}

tasks.register<Copy>("copyBanManagerBukkitJar") {
    group = "verification"
    description = "Copy BanManager Bukkit shadow JAR to e2e/jars"

    into(file("jars"))
    from(file("${banManagerDir.absolutePath}/bukkit/build/libs/BanManagerBukkit.jar"))
}

fun registerCopyBanManagerFabricTask(mcVersion: String) {
    tasks.register<Copy>("copyBanManagerFabric_${mcVersion.replace(".", "_")}Jar") {
        group = "verification"
        description = "Copy BanManager Fabric $mcVersion JAR to e2e/jars"

        into(file("jars"))
        from(file("${banManagerDir.absolutePath}/fabric/versions/$mcVersion/build/libs/BanManagerFabric-mc$mcVersion.jar"))
    }
}

registerCopyBanManagerFabricTask("1.20.1")
registerCopyBanManagerFabricTask("1.21.1")
registerCopyBanManagerFabricTask("1.21.4")

tasks.register<Copy>("copyWebEnhancerBukkitJar") {
    group = "verification"
    description = "Copy WebEnhancer Bukkit JAR to e2e/jars"

    dependsOn(":BanManagerWebEnhancerBukkit:shadowJar")

    into(file("jars"))
    from(project(":BanManagerWebEnhancerBukkit").tasks.named("shadowJar")) {
        rename { "BanManagerWebEnhancerBukkit.jar" }
    }
}

fun registerCopyWebEnhancerFabricTask(mcVersion: String) {
    tasks.register<Copy>("copyWebEnhancerFabric_${mcVersion.replace(".", "_")}Jar") {
        group = "verification"
        description = "Copy WebEnhancer Fabric $mcVersion JAR to e2e/jars"

        dependsOn(":fabric:${mcVersion}:remapJar")

        into(file("jars"))
        from(project(":fabric:${mcVersion}").tasks.named("remapJar")) {
            rename { "BanManagerWebEnhancerFabric-mc${mcVersion}.jar" }
        }
    }
}

registerCopyWebEnhancerFabricTask("1.20.1")
registerCopyWebEnhancerFabricTask("1.21.1")
registerCopyWebEnhancerFabricTask("1.21.4")

tasks.register("prepareJars") {
    group = "verification"
    description = "Prepare all plugin JARs for E2E tests"
    dependsOn("copyBanManagerBukkitJar")
}

data class FabricVersion(val mcVersion: String, val javaImage: String, val fabricLoader: String)

val fabricVersions = listOf(
    FabricVersion("1.20.1", "java17", "0.16.10"),
    FabricVersion("1.21.1", "java21", "0.16.9"),
    FabricVersion("1.21.4", "java21", "0.16.9")
)

fun createPlatformTestTask(
    taskName: String,
    platformDir: String,
    pluginTask: String,
    description: String,
    environment: Map<String, String> = emptyMap()
) {
    tasks.register<Exec>(taskName) {
        group = "verification"
        this.description = description

        dependsOn(pluginTask)

        workingDir = file("platforms/$platformDir")

        environment.forEach { (key, value) ->
            environment(key, value)
        }

        commandLine(
            "docker", "compose", "up",
            "--build",
            "--abort-on-container-exit",
            "--exit-code-from", "tests"
        )

        doLast {
            exec {
                workingDir = file("platforms/$platformDir")
                commandLine("docker", "compose", "down", "-v")
                isIgnoreExitValue = true
            }
        }
    }
}

tasks.register("prepareBukkitJars") {
    group = "verification"
    description = "Prepare Bukkit plugin JARs for E2E tests"
    dependsOn("copyBanManagerBukkitJar", "copyWebEnhancerBukkitJar")
}

createPlatformTestTask(
    "testBukkit",
    "bukkit",
    "prepareBukkitJars",
    "Run Bukkit E2E tests in Docker"
)

fabricVersions.forEach { version ->
    val versionSuffix = version.mcVersion.replace(".", "_")

    tasks.register("prepareFabric_${versionSuffix}Jars") {
        group = "verification"
        description = "Prepare Fabric ${version.mcVersion} plugin JARs for E2E tests"
        dependsOn("copyBanManagerFabric_${versionSuffix}Jar", "copyWebEnhancerFabric_${versionSuffix}Jar")
    }

    createPlatformTestTask(
        "testFabric_${versionSuffix}",
        "fabric",
        "prepareFabric_${versionSuffix}Jars",
        "Run Fabric ${version.mcVersion} E2E tests in Docker",
        mapOf(
            "MC_VERSION" to version.mcVersion,
            "JAVA_IMAGE" to version.javaImage,
            "FABRIC_LOADER" to version.fabricLoader
        )
    )
}

createPlatformTestTask(
    "testFabric",
    "fabric",
    "prepareFabric_1_21_4Jars",
    "Run Fabric E2E tests in Docker (latest: 1.21.4)",
    mapOf(
        "MC_VERSION" to "1.21.4",
        "JAVA_IMAGE" to "java21",
        "FABRIC_LOADER" to "0.16.9"
    )
)

tasks.register("testFabricAll") {
    group = "verification"
    description = "Run Fabric E2E tests for all supported MC versions"

    fabricVersions.forEach { version ->
        val versionSuffix = version.mcVersion.replace(".", "_")
        dependsOn("testFabric_${versionSuffix}")
    }
}

tasks.register("testAll") {
    group = "verification"
    description = "Run E2E tests for all platforms"

    dependsOn("testBukkit", "testFabric")
}

tasks.register("test") {
    group = "verification"
    description = "Run Bukkit E2E tests (alias for testBukkit)"
    dependsOn("testBukkit")
}

tasks.register<Exec>("startBukkit") {
    group = "verification"
    description = "Start the Bukkit test server without running tests (for debugging)"

    dependsOn("prepareBukkitJars")

    workingDir = file("platforms/bukkit")
    commandLine("docker", "compose", "up", "-d", "mariadb", "paper")
}

tasks.register<Exec>("stopBukkit") {
    group = "verification"
    description = "Stop the Bukkit test server"

    workingDir = file("platforms/bukkit")
    commandLine("docker", "compose", "down", "-v")
    isIgnoreExitValue = true
}

tasks.register<Exec>("logsBukkit") {
    group = "verification"
    description = "Show Bukkit server logs"

    workingDir = file("platforms/bukkit")
    commandLine("docker", "compose", "logs", "-f", "paper")
}

fun createFabricDebugTasks(mcVersion: String, javaImage: String, fabricLoader: String) {
    val versionSuffix = mcVersion.replace(".", "_")
    val envVars = mapOf(
        "MC_VERSION" to mcVersion,
        "JAVA_IMAGE" to javaImage,
        "FABRIC_LOADER" to fabricLoader
    )

    tasks.register<Exec>("startFabric_${versionSuffix}") {
        group = "verification"
        description = "Start the Fabric $mcVersion test server without running tests (for debugging)"

        dependsOn("prepareFabric_${versionSuffix}Jars")

        workingDir = file("platforms/fabric")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "up", "-d", "mariadb", "fabric")
    }

    tasks.register<Exec>("stopFabric_${versionSuffix}") {
        group = "verification"
        description = "Stop the Fabric $mcVersion test server"

        workingDir = file("platforms/fabric")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "down", "-v")
        isIgnoreExitValue = true
    }

    tasks.register<Exec>("logsFabric_${versionSuffix}") {
        group = "verification"
        description = "Show Fabric $mcVersion server logs"

        workingDir = file("platforms/fabric")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "logs", "-f", "fabric")
    }
}

fabricVersions.forEach { version ->
    createFabricDebugTasks(version.mcVersion, version.javaImage, version.fabricLoader)
}

tasks.register<Exec>("startFabric") {
    group = "verification"
    description = "Start the Fabric test server without running tests (for debugging) - latest: 1.21.4"

    dependsOn("prepareFabric_1_21_4Jars")

    workingDir = file("platforms/fabric")
    environment("MC_VERSION", "1.21.4")
    environment("JAVA_IMAGE", "java21")
    environment("FABRIC_LOADER", "0.16.9")
    commandLine("docker", "compose", "up", "-d", "mariadb", "fabric")
}

tasks.register<Exec>("stopFabric") {
    group = "verification"
    description = "Stop the Fabric test server"

    workingDir = file("platforms/fabric")
    commandLine("docker", "compose", "down", "-v")
    isIgnoreExitValue = true
}

tasks.register<Exec>("logsFabric") {
    group = "verification"
    description = "Show Fabric server logs"

    workingDir = file("platforms/fabric")
    commandLine("docker", "compose", "logs", "-f", "fabric")
}

tasks.named("clean") {
    doLast {
        listOf("bukkit", "fabric").forEach { platform ->
            exec {
                workingDir = file("platforms/$platform")
                commandLine("docker", "compose", "down", "-v", "--rmi", "local")
                isIgnoreExitValue = true
            }
        }
    }
}
