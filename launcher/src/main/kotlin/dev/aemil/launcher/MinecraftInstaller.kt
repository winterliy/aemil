package dev.aemil.launcher

import com.google.gson.JsonElement
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.Instant
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipFile

internal data class InstalledProfile(
    val gameVersion: String,
    val versionType: String,
    val fabricVersion: String,
    val profileId: String,
    val versionJson: Path,
    val clientJar: Path,
    val assetIndex: JsonObject,
    val loggingConfigPath: Path?,
    val nativeJars: List<NativeLibrary>,
)

internal data class NativeLibrary(val jar: Path, val exclusions: Set<String>)

internal object MinecraftInstaller {
    private const val MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private const val FABRIC_META_URL = "https://meta.fabricmc.net/v2/versions/loader"
    private val http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
    internal val operatingSystem: String by lazy {
        val name = System.getProperty("os.name").lowercase(Locale.ROOT)
        when {
            name.contains("win") -> "windows"
            name.contains("mac") || name.contains("darwin") -> "osx"
            name.contains("linux") -> "linux"
            else -> error("Unsupported operating system: $name")
        }
    }

    fun install(gameVersion: String, progress: (String, Int, Int) -> Unit): InstalledProfile {
        AemilPaths.ensureLayout()
        progress("Fetching Minecraft $gameVersion metadata", 0, 1)
        val manifest = getJson(URI.create(MANIFEST_URL))
        val versionEntry = manifest.getAsJsonArray("versions").firstOrNull { entry ->
            entry.asJsonObject.get("id")?.asString == gameVersion
        }?.asJsonObject ?: error("Minecraft version $gameVersion was not found in Mojang's version manifest")
        val vanilla = getJson(URI.create(versionEntry.get("url").asString), versionEntry.get("sha1")?.asString)
        val vanillaDir = AemilPaths.minecraftRoot.resolve("versions").resolve(gameVersion)
        Files.createDirectories(vanillaDir)
        val vanillaJsonPath = vanillaDir.resolve("$gameVersion.json")
        writeJson(vanillaJsonPath, vanilla)

        val client = vanilla.getAsJsonObject("downloads")?.getAsJsonObject("client")
            ?: error("Minecraft $gameVersion has no client download")
        val clientPath = vanillaDir.resolve("$gameVersion.jar")

        progress("Finding a stable Fabric loader for $gameVersion", 0, 1)
        val loaders = getJsonArray(URI.create("$FABRIC_META_URL/$gameVersion"))
        val loader = loaders.firstOrNull { it.asJsonObject.getAsJsonObject("loader").get("stable")?.asBoolean == true }
            ?.asJsonObject?.getAsJsonObject("loader")?.get("version")?.asString
            ?: error("No stable Fabric loader is available for Minecraft $gameVersion")
        val fabricProfile = getJson(URI.create("$FABRIC_META_URL/$gameVersion/$loader/profile/json"))
        val profileId = fabricProfile.get("id")?.asString ?: error("Fabric profile did not include an ID")
        val fabricDir = AemilPaths.minecraftRoot.resolve("versions").resolve(profileId)
        Files.createDirectories(fabricDir)
        val profilePath = fabricDir.resolve("$profileId.json")
        writeJson(profilePath, fabricProfile)

        val assetIndexInfo = vanilla.getAsJsonObject("assetIndex") ?: error("Minecraft $gameVersion has no asset index")
        val assetIndexName = assetIndexInfo.get("id").asString
        val assetIndexPath = AemilPaths.minecraftRoot.resolve("assets/indexes/$assetIndexName.json")
        val assetIndex = downloadJson(
            URI.create(assetIndexInfo.get("url").asString),
            assetIndexPath,
            assetIndexInfo.get("sha1")?.asString,
        )
        val loggingFile = vanilla.getAsJsonObject("logging")?.getAsJsonObject("client")?.getAsJsonObject("file")
        val loggingConfigPath = loggingFile?.let { AemilPaths.minecraftRoot.resolve("assets/log_configs/${it.get("id").asString}") }

        val libraries = sequenceOf(vanilla.getAsJsonArray("libraries"), fabricProfile.getAsJsonArray("libraries"))
            .filterNotNull()
            .flatMap { it.asSequence().map(JsonElement::getAsJsonObject) }
            .toList()
        val artifacts = libraries.mapNotNull(::artifactFor)
        val natives = libraries.mapNotNull { library -> nativeFor(library) }
        val objects = assetIndex.getAsJsonObject("objects").entrySet().map { it.value.asJsonObject }
        val allDownloads = buildList {
            add(Download(client.get("url").asString, clientPath, client.get("sha1")?.asString))
            if (loggingFile != null && loggingConfigPath != null) {
                add(Download(loggingFile.get("url").asString, loggingConfigPath, loggingFile.get("sha1")?.asString))
            }
            addAll(artifacts)
            natives.forEach { (download, _) -> add(download) }
            objects.forEach { asset ->
                val hash = asset.get("hash").asString
                val objectPath = AemilPaths.minecraftRoot.resolve("assets/objects/${hash.take(2)}/$hash")
                add(Download("https://resources.download.minecraft.net/${hash.take(2)}/$hash", objectPath, hash))
            }
        }.distinctBy { it.target }

        val finished = AtomicInteger(0)
        val total = allDownloads.size
        progress("Downloading Minecraft files (0/$total)", 0, total)
        val pool = Executors.newFixedThreadPool(8)
        try {
            val futures = allDownloads.map { download ->
                pool.submit {
                    download(download)
                    val completed = finished.incrementAndGet()
                    progress("Downloading Minecraft files ($completed/$total)", completed, total)
                }
            }
            futures.forEach { it.get() }
        } catch (failure: Exception) {
            throw (failure.cause ?: failure)
        } finally {
            pool.shutdownNow()
        }

        materializeLegacyAssets(assetIndex, gameVersion, assetIndexInfo.get("map_to_resources")?.asBoolean == true)

        val installedNatives = natives.map { (download, exclusions) -> NativeLibrary(download.target, exclusions) }
        AemilPaths.ensureGameLayout(gameVersion)
        AemilPaths.synchronizeUserMods(gameVersion)
        registerLauncherProfile(gameVersion, profileId, assetIndex)
        return InstalledProfile(gameVersion, versionEntry.get("type").asString, loader, profileId, profilePath, clientPath, assetIndex, loggingConfigPath, installedNatives)
    }

    private fun registerLauncherProfile(gameVersion: String, profileId: String, assetIndex: JsonObject) {
        val launcherProfiles = AemilPaths.minecraftRoot.resolve("launcher_profiles.json")
        val root = if (Files.exists(launcherProfiles)) {
            Files.newBufferedReader(launcherProfiles).use { JsonParser.parseReader(it).asJsonObject }
        } else {
            JsonObject()
        }
        val profiles = root.getAsJsonObject("profiles") ?: JsonObject().also { root.add("profiles", it) }
        val profileName = "Aemil Fabric $gameVersion"
        val profile = profiles.getAsJsonObject(profileName) ?: JsonObject().also { created ->
            val now = Instant.now().toString()
            created.addProperty("name", profileName)
            created.addProperty("type", "custom")
            created.addProperty("created", now)
            profiles.add(profileName, created)
        }
        profile.addProperty("lastVersionId", profileId)
        profile.addProperty("lastUsed", Instant.now().toString())
        wheatIcon(assetIndex)?.let { profile.addProperty("icon", it) }
        val temporary = launcherProfiles.resolveSibling("${launcherProfiles.fileName}.tmp")
        Files.writeString(temporary, GsonBuilder().setPrettyPrinting().create().toJson(root))
        try {
            Files.move(temporary, launcherProfiles, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(temporary, launcherProfiles, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    fun extractNatives(profile: InstalledProfile): Path {
        val directory = AemilPaths.minecraftRoot.resolve("versions/${profile.profileId}/natives")
        Files.createDirectories(directory)
        profile.nativeJars.forEach { native ->
            ZipFile(native.jar.toFile()).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.isDirectory || entry.name.startsWith("META-INF/") || native.exclusions.any(entry.name::startsWith)) continue
                    val output = directory.resolve(entry.name).normalize()
                    require(output.startsWith(directory)) { "Native archive contains an unsafe entry" }
                    Files.createDirectories(output.parent)
                    zip.getInputStream(entry).use { input -> Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING) }
                }
            }
        }
        return directory
    }

    private fun artifactFor(library: JsonObject): Download? {
        if (!allowed(library)) return null
        val downloads = library.getAsJsonObject("downloads")
        val artifact = downloads?.getAsJsonObject("artifact")
        if (artifact != null) {
            return Download(
                artifact.get("url").asString,
                AemilPaths.minecraftRoot.resolve("libraries/${artifact.get("path").asString}"),
                artifact.get("sha1")?.asString,
            )
        }
        val name = library.get("name")?.asString ?: return null
        val (path, _) = mavenPath(name)
        val base = library.get("url")?.asString ?: "https://libraries.minecraft.net/"
        return Download(base.trimEnd('/') + "/" + path, AemilPaths.minecraftRoot.resolve("libraries/$path"), library.get("sha1")?.asString)
    }

    internal fun isLibraryAllowed(library: JsonObject): Boolean = allowed(library)

    internal fun libraryPath(library: JsonObject): Path? {
        val artifact = library.getAsJsonObject("downloads")?.getAsJsonObject("artifact")
        if (artifact != null) return AemilPaths.minecraftRoot.resolve("libraries/${artifact.get("path").asString}")
        val name = library.get("name")?.asString ?: return null
        val (path, _) = mavenPath(name)
        return AemilPaths.minecraftRoot.resolve("libraries/$path")
    }

    private fun nativeFor(library: JsonObject): Pair<Download, Set<String>>? {
        if (!allowed(library)) return null
        val classifierTemplate = library.getAsJsonObject("natives")?.get(operatingSystem)?.asString ?: return null
        val classifier = classifierTemplate.replace("\${arch}", if (System.getProperty("os.arch").contains("64")) "64" else "32")
        val nativeArtifact = library.getAsJsonObject("downloads")?.getAsJsonObject("classifiers")?.getAsJsonObject(classifier)
        if (nativeArtifact != null) {
            val path = nativeArtifact.get("path").asString
            val url = nativeArtifact.get("url").asString
            val exclusions = library.getAsJsonObject("extract")?.getAsJsonArray("exclude")
                ?.map { it.asString }?.toSet().orEmpty()
            return Download(url, AemilPaths.minecraftRoot.resolve("libraries/$path"), nativeArtifact.get("sha1")?.asString) to exclusions
        }
        val name = library.get("name")?.asString ?: return null
        val (path, _) = mavenPath(name.substringBefore('@') + ":$classifier")
        val base = library.get("url")?.asString ?: "https://libraries.minecraft.net/"
        val target = AemilPaths.minecraftRoot.resolve("libraries/$path")
        return Download(base.trimEnd('/') + "/" + path, target, null) to emptySet()
    }

    private fun mavenPath(coordinate: String): Pair<String, String> {
        val nameAndExtension = coordinate.substringAfter('@', "jar")
        val parts = coordinate.substringBefore('@').split(':')
        require(parts.size >= 3) { "Invalid Maven coordinate: $coordinate" }
        val group = parts[0].replace('.', '/')
        val artifact = parts[1]
        val version = parts[2]
        val classifier = parts.getOrNull(3)?.let { "-$it" }.orEmpty()
        val file = "$artifact-$version$classifier.$nameAndExtension"
        return "$group/$artifact/$version/$file" to nameAndExtension
    }

    private fun allowed(library: JsonObject): Boolean {
        val rules = library.getAsJsonArray("rules") ?: return true
        var result = false
        rules.forEach { element ->
            val rule = element.asJsonObject
            val os = rule.getAsJsonObject("os")
            val matches = os == null || (
                (os.get("name") == null || os.get("name").asString == operatingSystem) &&
                    (os.get("arch") == null || os.get("arch").asString.toRegex().containsMatchIn(System.getProperty("os.arch"))) &&
                    (os.get("version") == null || os.get("version").asString.toRegex().containsMatchIn(System.getProperty("os.version")))
                )
            if (matches) result = rule.get("action")?.asString == "allow"
        }
        return result
    }

    private fun downloadJson(uri: URI, target: Path, sha1: String?): JsonObject {
        download(Download(uri.toString(), target, sha1))
        return Files.newBufferedReader(target).use { JsonParser.parseReader(it).asJsonObject }
    }

    private fun materializeLegacyAssets(assetIndex: JsonObject, gameVersion: String, mapToResources: Boolean) {
        val virtualName = assetIndex.get("virtual")?.asString
        val mapAssetsToResources = mapToResources || assetIndex.get("map_to_resources")?.asBoolean == true
        if (virtualName == null && !mapAssetsToResources) return
        assetIndex.getAsJsonObject("objects").entrySet().forEach { (name, assetElement) ->
            val hash = assetElement.asJsonObject.get("hash").asString
            val source = AemilPaths.minecraftRoot.resolve("assets/objects/${hash.take(2)}/$hash")
            val targets = buildList {
                if (virtualName != null) add(AemilPaths.minecraftRoot.resolve("assets/virtual/$virtualName/$name"))
                if (mapAssetsToResources) add(AemilPaths.gameDirectory(gameVersion).resolve("resources/$name"))
            }
            targets.forEach { target ->
                val normalized = target.normalize()
                val base = if (target.startsWith(AemilPaths.minecraftRoot.resolve("assets/"))) AemilPaths.minecraftRoot.resolve("assets")
                else AemilPaths.gameDirectory(gameVersion)
                require(normalized.startsWith(base)) { "Asset index contains an unsafe path" }
                Files.createDirectories(normalized.parent)
                if (Files.notExists(normalized)) {
                    runCatching { Files.createLink(normalized, source) }
                        .getOrElse { Files.copy(source, normalized) }
                }
            }
        }
    }

    private fun getJson(uri: URI, expectedSha1: String? = null): JsonObject {
        return getJsonElement(uri, expectedSha1).asJsonObject
    }

    private fun getJsonArray(uri: URI, expectedSha1: String? = null): JsonArray {
        return getJsonElement(uri, expectedSha1).asJsonArray
    }

    private fun getJsonElement(uri: URI, expectedSha1: String? = null): JsonElement {
        val request = HttpRequest.newBuilder(uri).header("User-Agent", "AemilLauncher/0.1").GET().build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofByteArray())
        check(response.statusCode() in 200..299) { "Request failed (${response.statusCode()}): $uri" }
        val body = response.body()
        if (expectedSha1 != null) {
            val actual = MessageDigest.getInstance("SHA-1").digest(body).joinToString("") { "%02x".format(it) }
            check(actual.equals(expectedSha1, ignoreCase = true)) { "SHA-1 mismatch for metadata at $uri" }
        }
        return JsonParser.parseString(body.toString(Charsets.UTF_8))
    }

    private fun wheatIcon(assetIndex: JsonObject): String? {
        val hash = assetIndex.getAsJsonObject("objects")
            ?.get("minecraft/textures/item/wheat.png")
            ?.asJsonObject?.get("hash")?.asString ?: return null
        val icon = AemilPaths.minecraftRoot.resolve("assets/objects/${hash.take(2)}/$hash")
        if (Files.notExists(icon)) return null
        return java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(icon))
    }

    private fun writeJson(path: Path, json: JsonObject) {
        Files.createDirectories(path.parent)
        Files.writeString(path, json.toString())
    }

    private data class Download(val url: String, val target: Path, val sha1: String?)

    private fun download(download: Download) {
        Files.createDirectories(download.target.parent)
        if (Files.exists(download.target) && validSha1(download.target, download.sha1)) return
        val temporary = download.target.resolveSibling("${download.target.fileName}.part")
        Files.deleteIfExists(temporary)
        val request = HttpRequest.newBuilder(URI.create(download.url)).header("User-Agent", "AemilLauncher/0.1").GET().build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofFile(temporary))
        if (response.statusCode() !in 200..299) {
            Files.deleteIfExists(temporary)
            error("Download failed (${response.statusCode()}): ${download.url}")
        }
        check(validSha1(temporary, download.sha1)) { "SHA-1 mismatch for ${download.target.fileName}" }
        try {
            Files.move(temporary, download.target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(temporary, download.target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun validSha1(path: Path, expected: String?): Boolean {
        if (expected == null) return Files.exists(path)
        val digest = MessageDigest.getInstance("SHA-1")
        Files.newInputStream(path).use { input: InputStream ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }.equals(expected, ignoreCase = true)
    }
}
