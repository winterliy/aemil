plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    application
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

application {
    mainClass = "dev.aemil.launcher.MainKt"
}

compose.desktop {
    application {
        mainClass = "dev.aemil.launcher.MainKt"
        nativeDistributions {
            packageName = "Aemil"
            packageVersion = project.version.toString().substringBefore('-')
            targetPlatforms(org.jetbrains.compose.desktop.application.dsl.TargetPlatform.MacOSArm64,
                org.jetbrains.compose.desktop.application.dsl.TargetPlatform.MacOSX64,
                org.jetbrains.compose.desktop.application.dsl.TargetPlatform.WindowsX64)
        }
    }
}
