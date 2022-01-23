import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyLibrariesConfiguration()

dependencies {
    "shade"("de.mkammerer:argon2-jvm:2.11")
    "shade"("com.google.guava:guava:17.0")
    "shade"("org.apache.logging.log4j:log4j-core:2.17.0")
    "shade"("org.apache.logging.log4j:log4j-api:2.17.0")
}

tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.named<ShadowJar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN

    dependencies {
        relocate("de.mkammerer.argon2", "me.confuser.banmanager.webenhancer.common.argon2") {
            include(dependency("de.mkammerer:argon2-jvm"))
            include(dependency("net.java.dev.jna:jna"))
        }

        relocate("com.google.common", "me.confuser.banmanager.webenhancer.common.google.guava") {
            include(dependency("com.google.guava:guava"))
        }

        include(dependency("org.apache.logging.log4j:log4j-core"))
        include(dependency("org.apache.logging.log4j:log4j-api"))
    }

    exclude("GradleStart**")
    exclude(".cache");
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
}
