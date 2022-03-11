import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency


plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin")
    id("net.kyori.blossom") version "1.2.0"
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

blossom {
    replaceToken("@projectVersion@", project.ext["internalVersion"])
}

sponge {
    apiVersion("7.2.0")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }

    license("MIT License")

    plugin("banmanager-webenhancer") {
        displayName("BanManager-WebEnhancer")
        entrypoint("me.confuser.banmanager.webenhancer.sponge.SpongePlugin")
        description("An addon required by the BanManager WebUI")
        links {
            homepage("https://banmanagement.com/")
            source("https://github.com/BanManagment/BanManager-WebEnhancer")
            issues("https://github.com/BanManagment/BanManager-WebEnhancer")
        }
        contributor("confuser") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
            version("7.2.0")
        }
        dependency("banmanager") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
            version("7.7.0")
        }
    }
}

repositories {
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/maven/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:7.2.0")
    compileOnly("me.confuser.banmanager:BanManagerSponge:7.7.0")

    api(project(":BanManagerWebEnhancerCommon")) {
        isTransitive = true
    }

    "shadeOnly"("org.bstats:bstats-sponge:2.2.1")
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
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
        relocate("org.bstats", "me.confuser.banmanager.webenhancer.common.bstats") {
            include(dependency("org.bstats:"))
        }
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

    minimize()
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
