package dev.aemil.launcher

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal data class UserMod(val path: Path, val enabled: Boolean) {
    val displayName: String
        get() = path.fileName.toString().removeSuffix(".disabled").removeSuffix(".jar")
}

internal object UserModsRepository {
    fun list(version: String? = null): List<UserMod> {
        AemilPaths.ensureLayout()
        val directory = version?.let { AemilPaths.gameDirectory(it).resolve("mods") } ?: AemilPaths.mods
        Files.createDirectories(directory)
        return Files.list(directory).use { files ->
            files.filter { path ->
                val name = path.fileName.toString()
                Files.isRegularFile(path) && name !in setOf("aemil-loader.jar", "aemil-client.jar", "aemil-phosphor-fabric-1.19.x.jar", "fabric-api.jar") &&
                    (name.endsWith(".jar") || name.endsWith(".jar.disabled"))
            }.map { path -> UserMod(path, path.fileName.toString().endsWith(".jar")) }
                .sorted(compareBy(String.CASE_INSENSITIVE_ORDER) { it.path.fileName.toString() })
                .toList()
        }
    }

    fun setEnabled(mod: UserMod, enabled: Boolean) {
        if (mod.enabled == enabled) return
        val source = mod.path
        val target = if (enabled) source.resolveSibling(source.fileName.toString().removeSuffix(".disabled"))
        else source.resolveSibling("${source.fileName}.disabled")
        if (Files.exists(target)) return
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(source, target)
        }
    }
}
