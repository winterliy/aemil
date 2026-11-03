package dev.aemil.launcher

import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path

internal object AemilPaths {
    private val os = System.getProperty("os.name").lowercase()
    private val home = Path.of(System.getProperty("user.home"))

    val minecraftRoot: Path = when {
        os.contains("mac") -> home.resolve("Library/Application Support/minecraft")
        os.contains("win") -> Path.of(System.getenv("APPDATA") ?: home.resolve("AppData/Roaming").toString()).resolve(".minecraft")
        else -> home.resolve(".minecraft")
    }
    val root: Path = minecraftRoot.resolve("aemil")
    private val mods = root.resolve("mods")

    fun ensureLayout() {
        listOf("updates", "profiles", "mods", "config", "logs", "versions", "libraries", "assets")
            .forEach { Files.createDirectories(root.resolve(it)) }
    }

    fun openModsFolder() {
        ensureLayout()
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(mods.toFile())
    }
}
