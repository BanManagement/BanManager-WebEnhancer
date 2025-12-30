plugins {
    `java-library`
    jacoco
}

applyPlatformAndCoreConfiguration()

dependencies {
    api(project(":BanManagerWebEnhancerLibs"))

    api("me.confuser.banmanager:BanManagerCommon:7.10.0-SNAPSHOT")
    api("me.confuser.banmanager.BanManagerLibs:BanManagerLibs:7.10.0-SNAPSHOT")

    // Test dependencies
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:4.0.0")
    testImplementation("org.powermock:powermock-module-junit4:2.0.2")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.2")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:2.6.0")
    testImplementation("org.awaitility:awaitility:4.0.1")
}

tasks.withType<Test>().configureEach {
    useJUnit()
    maxHeapSize = "512m"
    forkEvery = 1  // Fork a new JVM for each test class to prevent memory accumulation
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}
