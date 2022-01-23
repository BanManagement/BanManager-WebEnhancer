plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()

dependencies {
    api(project(":BanManagerWebEnhancerLibs"))

    api("me.confuser.banmanager:BanManagerCommon:7.7.0-SNAPSHOT")
    api("me.confuser.banmanager.BanManagerLibs:BanManagerLibs:7.7.0-SNAPSHOT")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

tasks.withType<Test>().configureEach {
    useJUnit()
}
