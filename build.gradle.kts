plugins {
    kotlin("jvm") version "2.4.21" apply false
    id("org.jetbrains.compose") version "1.12.2" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
    id("net.fabricmc.fabric-loom") version "1.19-SNAPSHOT" apply false
}

allprojects {
    group = "dev.aemil"
    version = "0.1.0-alpha.3"
}
