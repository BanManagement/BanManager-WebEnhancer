import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyLibrariesConfiguration()

dependencies {
    "shade"("org.bouncycastle:bcprov-jdk15on:1.70")
    "shade"("com.google.guava:guava:33.4.8-jre")
}

tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.named<ShadowJar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN

    dependencies {
        relocate("org.bouncycastle", "me.confuser.banmanager.webenhancer.common.bouncycastle") {
            include(dependency("org.bouncycastle:bcprov-jdk15on"))
        }

        relocate("com.google.common", "me.confuser.banmanager.webenhancer.common.google.guava") {
            include(dependency("com.google.guava:guava"))
        }
    }

    exclude("GradleStart**")
    exclude(".cache");
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}
