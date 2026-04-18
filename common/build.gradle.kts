plugins {
    `java-library`
    jacoco
}

applyPlatformAndCoreConfiguration()

dependencies {
    api(project(":BanManagerWebEnhancerLibs"))

    api("me.confuser.banmanager:BanManagerCommon:8.0.0-SNAPSHOT")
    api("me.confuser.banmanager.BanManagerLibs:BanManagerLibs:8.0.0-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:3.3.1")
    testImplementation("org.awaitility:awaitility:4.3.0")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxHeapSize = "512m"
    // Fork a new JVM per test class - tests share JDBC/native fixtures that otherwise
    // accumulate state across runs and hang the suite. See BanManager/common build.
    forkEvery = 1
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}
