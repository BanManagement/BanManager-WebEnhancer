plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()

dependencies {
    api(project(":BanManagerWebEnhancerLibs"))

    api("me.confuser.banmanager:BanManagerCommon:7.7.0")
    api("me.confuser.banmanager.BanManagerLibs:BanManagerLibs:7.7.0")
}

tasks.withType<Test>().configureEach {
    useJUnit()
}
