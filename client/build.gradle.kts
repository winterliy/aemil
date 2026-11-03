plugins {
    kotlin("jvm")
    id("net.fabricmc.fabric-loom")
}

val minecraftVersion = providers.gradleProperty("minecraft_version")
val loaderVersion = providers.gradleProperty("loader_version")
val fabricApiVersion = providers.gradleProperty("fabric_api_version")
val fabricLanguageKotlinVersion = providers.gradleProperty("fabric_language_kotlin_version")
val javaVersion = providers.gradleProperty("java_version").get().toInt()

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion.get()}")
    implementation("net.fabricmc:fabric-loader:${loaderVersion.get()}")
    implementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion.get()}")
    implementation("net.fabricmc:fabric-language-kotlin:${fabricLanguageKotlinVersion.get()}")
    include("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion.get()}")
    include("net.fabricmc:fabric-language-kotlin:${fabricLanguageKotlinVersion.get()}")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion.toString()))
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
}

loom {
    splitEnvironmentSourceSets()
    mods {
        register("aemil") {
            sourceSet(sourceSets.main.get())
            sourceSet("client")
        }
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
