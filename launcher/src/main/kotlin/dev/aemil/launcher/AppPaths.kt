package dev.aemil.launcher

import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal object AemilPaths {
    private val os = System.getProperty("os.name").lowercase()
    private val home = Path.of(System.getProperty("user.home"))

    val minecraftRoot: Path = when {
        os.contains("mac") -> home.resolve("Library/Application Support/minecraft")
        os.contains("win") -> Path.of(System.getenv("APPDATA") ?: home.resolve("AppData/Roaming").toString()).resolve(".minecraft")
        else -> home.resolve(".minecraft")
    }
    val root: Path = minecraftRoot.resolve("aemil")
    val mods = root.resolve("mods")

    fun ensureLayout() {
        listOf("updates", "profiles", "mods", "config", "logs", "state")
            .forEach { Files.createDirectories(root.resolve(it)) }

        val modMenuConfig = root.resolve("config/modmenu.json")
        if (Files.notExists(modMenuConfig)) {
            Files.writeString(modMenuConfig, "{\"update_checker\":false}\n")
        }
    }

    fun gameDirectory(version: String): Path = minecraftRoot

    fun ensureGameLayout(version: String) {
        val gameDirectory = gameDirectory(version)
        listOf("mods", "config", "logs", "resourcepacks", "resources", "saves").forEach { Files.createDirectories(gameDirectory.resolve(it)) }
        val modMenuConfig = gameDirectory.resolve("config/modmenu.json")
        val config = if (Files.exists(modMenuConfig)) {
            Files.newBufferedReader(modMenuConfig).use { com.google.gson.JsonParser.parseReader(it).asJsonObject }
        } else {
            com.google.gson.JsonObject()
        }
        config.addProperty("update_checker", false)
        val hiddenMods = config.getAsJsonArray("hidden_mods") ?: com.google.gson.JsonArray().also { config.add("hidden_mods", it) }
        if (hiddenMods.none { it.asString == "aemil-loader" }) hiddenMods.add("aemil-loader")
        Files.createDirectories(modMenuConfig.parent)
        Files.writeString(modMenuConfig, com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(config))
    }

    fun installAemilLoader(version: String): Path? {
        if (version != "26.3") return null
        val loaderJar = root.resolve("builtin-mods/aemil-loader.jar")
        Files.createDirectories(loaderJar.parent)
        val resource = AemilPaths::class.java.getResourceAsStream("/builtin/aemil-loader.jar")
            ?: error("The built-in Aemil Loader JAR is missing from the launcher")
        val temporary = loaderJar.resolveSibling("${loaderJar.fileName}.tmp")
        resource.use { input -> Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING) }
        try {
            Files.move(temporary, loaderJar, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(temporary, loaderJar, StandardCopyOption.REPLACE_EXISTING)
        }
        return loaderJar
    }

    private fun installPhosphor(version: String): Path? {
        if (version !in setOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4")) {
            return null
        }
        val phosphorJar = root.resolve("builtin-mods/aemil-phosphor-fabric-1.19.x.jar")
        Files.createDirectories(phosphorJar.parent)
        val resource = AemilPaths::class.java.getResourceAsStream("/builtin/phosphor-fabric-1.19.x.jar")
            ?: error("The built-in Phosphor JAR is missing from the launcher")
        val temporary = phosphorJar.resolveSibling("${phosphorJar.fileName}.tmp")
        resource.use { input -> Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING) }
        try {
            Files.move(temporary, phosphorJar, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(temporary, phosphorJar, StandardCopyOption.REPLACE_EXISTING)
        }
        return phosphorJar
    }

    fun synchronizeUserMods(version: String) {
        val instanceMods = minecraftRoot.resolve("mods")
        Files.createDirectories(instanceMods)
        val managedList = root.resolve("state/managed-mods.txt")
        val previouslyManaged = if (Files.exists(managedList)) {
            Files.readAllLines(managedList).filter { name ->
                name.isNotBlank() && Path.of(name).fileName.toString() == name && name.endsWith(".jar", ignoreCase = true)
            }.toSet()
        } else emptySet()
        val desired = linkedMapOf<String, Path>()
        UserModsRepository.list().filter(UserMod::enabled).forEach { mod ->
            val name = mod.path.fileName.toString().removeSuffix(".disabled")
            require(name !in setOf("aemil-loader.jar", "aemil-client.jar", "aemil-phosphor-fabric-1.19.x.jar")) {
                "The filename $name is reserved for an Aemil built-in component"
            }
            desired[name] = mod.path
        }
        installAemilLoader(version)?.let { desired[it.fileName.toString()] = it }
        installPhosphor(version)?.let { desired[it.fileName.toString()] = it }

        previouslyManaged.filterNot(desired::containsKey).forEach { name ->
            Files.deleteIfExists(instanceMods.resolve(name))
        }
        desired.forEach { (name, source) ->
            val target = instanceMods.resolve(name)
            if (source != target) {
                require(!Files.exists(target) || name in previouslyManaged) {
                    "Cannot install $name because a file with that name already exists in ${instanceMods}. Move or rename the existing file first."
                }
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
        Files.createDirectories(managedList.parent)
        val temporary = managedList.resolveSibling("${managedList.fileName}.tmp")
        Files.write(temporary, desired.keys.sorted())
        try {
            Files.move(temporary, managedList, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(temporary, managedList, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    fun openModsFolder() {
        ensureLayout()
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(mods.toFile())
    }

    fun openLogsFolder(version: String) {
        ensureGameLayout(version)
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(gameDirectory(version).resolve("logs").toFile())
    }
}
