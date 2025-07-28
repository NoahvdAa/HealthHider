plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "HealthHider"
