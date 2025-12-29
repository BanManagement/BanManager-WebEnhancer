import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

sponge {
    apiVersion("11.0.0")
    loader {
        name(org.spongepowered.gradle.plugin.config.PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("MIT License")

    plugin("banmanager-webenhancer") {
        displayName("BanManager-WebEnhancer")
        entrypoint("me.confuser.banmanager.webenhancer.sponge.SpongePlugin")
        description("An addon required by the BanManager WebUI")
        links {
            homepage("https://banmanagement.com/")
            source("https://github.com/BanManagement/BanManager-WebEnhancer")
            issues("https://github.com/BanManagement/BanManager-WebEnhancer/issues")
        }
        contributor("confuser") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(org.spongepowered.plugin.metadata.model.PluginDependency.LoadOrder.AFTER)
            optional(false)
            version("11.0.0")
        }
        dependency("banmanager") {
            loadOrder(org.spongepowered.plugin.metadata.model.PluginDependency.LoadOrder.AFTER)
            optional(false)
            version("7.10.0")
        }
    }
}

repositories {
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/repository/maven-public/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:11.0.0")
    compileOnly("me.confuser.banmanager:BanManagerSponge:7.10.0-SNAPSHOT")

    api(project(":BanManagerWebEnhancerCommon")) {
        isTransitive = true
    }

    "shadeOnly"("org.bstats:bstats-sponge:3.0.2")
}

// Sponge API 11+ requires Java 21
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]

    inputs.property("internalVersion", internalVersion)

    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion, "mainPath" to "me.confuser.banmanager.webenhancer.sponge.SpongePlugin")
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

    archiveBaseName.set("BanManagerWebEnhancerSponge")
    archiveClassifier.set("")
    archiveVersion.set("")

    dependencies {
        include(dependency(":BanManagerWebEnhancerCommon"))
        include(dependency(":BanManagerWebEnhancerLibs"))
        include(dependency("org.bstats:.*:.*"))

        relocate("org.bstats", "me.confuser.banmanager.webenhancer.common.bstats")
    }

    exclude("GradleStart**")
    exclude(".cache");
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
    exclude("**/module-info.class")
    exclude("*.yml")
    exclude("org/apache/logging/**")
    exclude("Log4j-**")
    exclude("META-INF/org/apache/logging/**")
    exclude("com/google/**")

    minimize {
        exclude(dependency("org.bstats:.*:.*"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
