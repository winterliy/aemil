package dev.aemil.launcher

import java.nio.file.Files
import java.util.Properties

internal object LauncherPreferences {
    private val settingsFile get() = AemilPaths.root.resolve("launcher.properties")

    fun language(): AppLanguage {
        val values = Properties()
        if (Files.exists(settingsFile)) Files.newInputStream(settingsFile).use(values::load)
        return if (values.getProperty("language") == "ko") AppLanguage.KOREAN else AppLanguage.ENGLISH
    }

    fun setLanguage(language: AppLanguage) {
        properties().apply { setProperty("language", if (language == AppLanguage.KOREAN) "ko" else "en") }.save()
    }

    private fun properties() = Properties().apply {
        if (Files.exists(settingsFile)) Files.newInputStream(settingsFile).use(::load)
    }

    private fun Properties.save() {
        Files.createDirectories(settingsFile.parent)
        Files.newOutputStream(settingsFile).use { store(it, "Aemil launcher settings") }
    }
}
