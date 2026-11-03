plugins {
    kotlin("jvm")
    id("net.fabricmc.fabric-loom")
}

repositories {
    maven("https://maven.architectury.dev/")
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://cursemaven.com/")
}

val minecraftVersion = providers.gradleProperty("minecraft_version")
val loaderVersion = providers.gradleProperty("loader_version")
val fabricApiVersion = providers.gradleProperty("fabric_api_version")
val fabricLanguageKotlinVersion = providers.gradleProperty("fabric_language_kotlin_version")
val modMenuVersion = providers.gradleProperty("modmenu_version")
val dynamicFpsCurseFile = providers.gradleProperty("dynamic_fps_curse_file")
val clothConfigCurseFile = providers.gradleProperty("cloth_config_curse_file")
val ferriteCoreCurseFile = providers.gradleProperty("ferritecore_curse_file")
val lithiumCurseFile = providers.gradleProperty("lithium_curse_file")
val kryptonCurseFile = providers.gradleProperty("krypton_curse_file")
val continuityCurseFile = providers.gradleProperty("continuity_curse_file")
val sodiumCurseFile = providers.gradleProperty("sodium_curse_file")
val irisCurseFile = providers.gradleProperty("iris_curse_file")
val architecturyVersion = providers.gradleProperty("architectury_version")
val forgeConfigApiPortVersion = providers.gradleProperty("forge_config_api_port_version")
val javaVersion = providers.gradleProperty("java_version").get().toInt()

val dynamicFps = "curse.maven:dynamic-fps-335493:${dynamicFpsCurseFile.get()}"
val clothConfig = "curse.maven:cloth-config-348521:${clothConfigCurseFile.get()}"
val ferriteCore = "curse.maven:ferritecore-fabric-459857:${ferriteCoreCurseFile.get()}"
val lithium = "curse.maven:lithium-360438:${lithiumCurseFile.get()}"
val krypton = "curse.maven:krypton-428912:${kryptonCurseFile.get()}"
val continuity = "curse.maven:continuity-531351:${continuityCurseFile.get()}"
val sodium = "curse.maven:sodium-394468:${sodiumCurseFile.get()}"
val iris = "curse.maven:irisshaders-455508:${irisCurseFile.get()}"
val modMenu = "com.terraformersmc:modmenu:${modMenuVersion.get()}"
val architectury = "dev.architectury:architectury-fabric:${architecturyVersion.get()}"
val forgeConfigApiPort = "fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${forgeConfigApiPortVersion.get()}"

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion.get()}")
    implementation("net.fabricmc:fabric-loader:${loaderVersion.get()}")
    implementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion.get()}")
    implementation("net.fabricmc:fabric-language-kotlin:${fabricLanguageKotlinVersion.get()}")
    implementation(dynamicFps)
    implementation(clothConfig)
    implementation(modMenu)
    implementation(ferriteCore)
    implementation(lithium)
    implementation(krypton)
    implementation(continuity)
    implementation(sodium)
    implementation(iris)
    implementation(architectury)
    implementation(forgeConfigApiPort)
    include("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion.get()}")
    include("net.fabricmc:fabric-language-kotlin:${fabricLanguageKotlinVersion.get()}")
    include(dynamicFps)
    include(clothConfig)
    include(modMenu)
    include(ferriteCore)
    include(lithium)
    include(krypton)
    include(continuity)
    include(sodium)
    include(iris)
    include(architectury)
    include(forgeConfigApiPort)
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
    from(rootProject.file("docs/THIRD_PARTY_NOTICES.md")) {
        into("META-INF")
        rename { "THIRD-PARTY-NOTICES.md" }
    }
    from(rootProject.file("docs/licenses")) {
        into("META-INF/licenses")
    }
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
