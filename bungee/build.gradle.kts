import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    api(project(":BanManagerWebEnhancerCommon")) {
        isTransitive = true
    }
    compileOnly("me.confuser.banmanager:BanManagerBungee:7.8.0-SNAPSHOT")

    compileOnly("net.md-5:bungeecord-api:1.14-SNAPSHOT")
    "shadeOnly"("org.bstats:bstats-bungeecord:2.2.1")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]

    inputs.property("internalVersion", internalVersion)

    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion, "mainPath" to "me.confuser.banmanager.webenhancer.bungee.BungeePlugin")
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

    archiveBaseName.set("BanManagerWebEnhancerBungeeCord")
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
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")

    minimize()
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
