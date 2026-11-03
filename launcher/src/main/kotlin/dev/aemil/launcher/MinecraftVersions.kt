package dev.aemil.launcher

import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal object MinecraftVersions {
    private val client = HttpClient.newBuilder().build()
    private val manifestUri = URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")

    fun fetch(): List<String> {
        val request = HttpRequest.newBuilder(manifestUri).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) { "Minecraft version manifest returned ${response.statusCode()}" }
        val versions = JsonParser.parseString(response.body()).asJsonObject.getAsJsonArray("versions")
        return versions.mapNotNull { item ->
            val entry = item.asJsonObject
            entry.get("id")?.asString?.takeIf { entry.get("type")?.asString in setOf("release", "snapshot") }
        }
    }
}
