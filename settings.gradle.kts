pluginManagement {
    repositories {
        google()
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        maven("https://maven.fabricmc.net/")
        mavenCentral()
    }
}

rootProject.name = "aemil"
include("launcher", "client")
