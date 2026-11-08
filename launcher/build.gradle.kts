import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.file.DuplicatesStrategy
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

repositories {
    google()
    maven("https://maven.fabricmc.net/")
    mavenCentral()
    maven("https://cursemaven.com/")
}

val phosphorBuiltin by configurations.creating {
    isTransitive = false
}
val fabricLoaderBuiltin by configurations.creating {
    isTransitive = false
}
val fabricApiBuiltin by configurations.creating {
    isTransitive = false
}

val minecraftDevVersion = providers.gradleProperty("minecraft_version")
val fabricLoaderDevVersion = providers.gradleProperty("loader_version")
val fabricApiDevVersion = providers.gradleProperty("fabric_api_version")

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.9.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    add(phosphorBuiltin.name, "curse.maven:phosphor-372124:3832074")
    add(fabricLoaderBuiltin.name, "net.fabricmc:fabric-loader:${fabricLoaderDevVersion.get()}")
    add(fabricApiBuiltin.name, "net.fabricmc.fabric-api:fabric-api:${fabricApiDevVersion.get()}")
}

val embeddedClientJar = project(":client").tasks.named<Jar>("jar")

tasks.processResources {
    dependsOn(embeddedClientJar)
    from(embeddedClientJar.map { it.archiveFile }) {
        into("builtin")
        rename { "aemil-loader.jar" }
    }
    from(phosphorBuiltin) {
        into("builtin")
        rename { "phosphor-fabric-1.19.x.jar" }
    }
    from(fabricLoaderBuiltin) {
        into("builtin/fabric/${minecraftDevVersion.get()}")
        rename { "fabric-loader.jar" }
    }
    from(fabricApiBuiltin) {
        into("builtin/fabric/${minecraftDevVersion.get()}")
        rename { "fabric-api.jar" }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.register<Jar>("uberJar") {
    group = "build"
    description = "Build a runnable, platform-specific Aemil launcher JAR."
    dependsOn(tasks.classes)
    archiveBaseName.set("aemil-launcher")
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest.attributes["Main-Class"] = "dev.aemil.launcher.MainKt"
    manifest.attributes["Enable-Native-Access"] = "ALL-UNNAMED"
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { dependency ->
        if (dependency.isDirectory) dependency else zipTree(dependency)
    })
    exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA", "META-INF/INDEX.LIST", "module-info.class")
}

compose.desktop {
    application {
        mainClass = "dev.aemil.launcher.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe)
            packageName = "Aemil"
            packageVersion = "0.1.${project.version.toString().substringAfterLast('.').toIntOrNull() ?: 0}"
            description = "Aemil Minecraft Launcher"
            vendor = "Aemil"
            modules("java.desktop", "java.net.http", "java.xml", "jdk.crypto.ec", "jdk.unsupported")
            macOS {
                bundleID = "dev.aemil.launcher"
                dockName = "Aemil"
                iconFile.set(project.file("src/main/packaging/icons/aemil-wheat.icns"))
            }
            windows {
                perUserInstall = true
                dirChooser = true
                menuGroup = "Aemil"
                upgradeUuid = "6cb0aaf9-80d9-40a0-8db6-97eacdd8cb18"
                iconFile.set(project.file("src/main/packaging/icons/aemil-wheat.ico"))
            }
        }
    }
}
