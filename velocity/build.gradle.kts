import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "velocity"
        url = uri("https://nexus.velocitypowered.com/repository/maven-public/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    api(project(":BanManagerWebEnhancerCommon")) {
        isTransitive = true
    }
    compileOnly("me.confuser.banmanager:BanManagerVelocity:7.10.0-SNAPSHOT")

    compileOnly("com.velocitypowered:velocity-api:3.1.0")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.0")
    "shadeOnly"("org.bstats:bstats-velocity:3.0.0")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    expand("internalVersion" to internalVersion)
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

    archiveBaseName.set("BanManagerWebEnhancerVelocity")
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
