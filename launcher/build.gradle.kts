import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.file.DuplicatesStrategy

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

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.9.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    add(phosphorBuiltin.name, "curse.maven:phosphor-372124:3832074")
}

val embeddedClientJar = project(":client").tasks.named<Jar>("jar")

tasks.processResources {
    dependsOn(embeddedClientJar)
    from(embeddedClientJar.map { it.archiveFile }) {
        into("builtin")
        rename { "aemil-client.jar" }
    }
    from(phosphorBuiltin) {
        into("builtin")
        rename { "phosphor-fabric-1.19.x.jar" }
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
    }
}
