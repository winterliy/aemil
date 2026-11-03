package dev.aemil.client.zoom

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.util.Properties
import kotlin.math.pow

object ZoomController {
    private const val STEP = 1.2
    private val configPath = FabricLoader.getInstance().configDir.resolve("aemil.properties")

    @Volatile private var held = false
    @Volatile private var minimum = 1.0
    @Volatile private var maximum = 32.0
    @Volatile private var default = 4.0
    @Volatile private var factor = default

    fun load() {
        val values = Properties()
        if (Files.exists(configPath)) Files.newInputStream(configPath).use(values::load)
        minimum = values.getProperty("zoom.minimum", "1.0").toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.0
        maximum = values.getProperty("zoom.maximum", "32.0").toDoubleOrNull()?.coerceAtLeast(minimum) ?: 32.0
        default = values.getProperty("zoom.default", "4.0").toDoubleOrNull()?.coerceIn(minimum, maximum) ?: 4.0.coerceIn(minimum, maximum)
        factor = default
        save()
    }

    @Synchronized
    fun setLimits(newMinimum: Double = minimum, newMaximum: Double = maximum, newDefault: Double = default) {
        minimum = newMinimum.coerceAtLeast(1.0)
        maximum = newMaximum.coerceAtLeast(minimum)
        default = newDefault.coerceIn(minimum, maximum)
        factor = factor.coerceIn(minimum, maximum)
        save()
    }

    @Synchronized
    fun updateKeyState(isDown: Boolean) {
        if (isDown && !held) factor = default
        held = isDown
    }

    fun isHeld(): Boolean = held
    fun minimum(): Double = minimum
    fun maximum(): Double = maximum
    fun default(): Double = default

    @Synchronized
    fun adjustFromScroll(amount: Double) {
        if (held && amount.isFinite() && amount != 0.0) {
            factor = (factor * STEP.pow(amount)).coerceIn(minimum, maximum)
        }
    }

    fun applyToFov(fov: Float): Float = if (held) (fov / factor.toFloat()).coerceAtLeast(1.0f) else fov

    private fun save() {
        Files.createDirectories(configPath.parent)
        val values = Properties().apply {
            setProperty("zoom.minimum", minimum.toString())
            setProperty("zoom.maximum", maximum.toString())
            setProperty("zoom.default", default.toString())
        }
        Files.newOutputStream(configPath).use { values.store(it, "Aemil client settings") }
    }
}
