rootProject.name = "BanManagerWebEnhancer"

include(":BanManagerWebEnhancerCommon")
include(":BanManagerWebEnhancerBukkit")
// include(":BanManagerWebEnhancerBungee")
include(":BanManagerWebEnhancerSponge")
include(":BanManagerWebEnhancerLibs")
//include("BanManagerWebEnhancerVelocity")

project(":BanManagerWebEnhancerCommon").projectDir = file("common")
project(":BanManagerWebEnhancerBukkit").projectDir = file("bukkit")
// project(":BanManagerWebEnhancerBungee").projectDir = file("bungee")
project(":BanManagerWebEnhancerSponge").projectDir = file("sponge")
project(":BanManagerWebEnhancerLibs").projectDir = file("libs")
//project("BanManagerVelocity").projectDir = file("velocity")
