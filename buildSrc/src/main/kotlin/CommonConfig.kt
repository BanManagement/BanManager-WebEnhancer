import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*

fun Project.applyCommonConfiguration() {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
        // Central Portal snapshot repository (OSSRH sunset June 2025)
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
        maven { url = uri("https://ci.frostcast.net/plugin/repository/everything") }
    }

    dependencies {
        "compileOnly"("org.projectlombok:lombok:1.18.36")
        "annotationProcessor"("org.projectlombok:lombok:1.18.36")

        "testCompileOnly"("org.projectlombok:lombok:1.18.36")
        "testAnnotationProcessor"("org.projectlombok:lombok:1.18.36")
    }

    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(5, "MINUTES")
        }
    }

    // Only set Java 1.8 for non-Fabric modules
    // Fabric uses toolchain configuration in its build.gradle.kts
    plugins.withId("java") {
        if (!plugins.hasPlugin("fabric-loom")) {
            the<JavaPluginExtension>().setSourceCompatibility("1.8")
            the<JavaPluginExtension>().setTargetCompatibility("1.8")
        }
    }
}
