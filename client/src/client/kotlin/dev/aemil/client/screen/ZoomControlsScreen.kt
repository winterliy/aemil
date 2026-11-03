package dev.aemil.client.screen

import dev.aemil.client.zoom.ZoomController
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class ZoomControlsScreen(private val controlsScreen: Screen) : Screen(Component.translatable("screen.aemil.zoom.title")) {
    override fun init() {
        val center = width / 2
        val rowWidth = 260
        val left = center - rowWidth / 2
        var y = height / 4

        addRenderableWidget(
            Button.builder(Component.translatable("screen.aemil.zoom.instructions"), Button.OnPress {})
                .bounds(left, y, rowWidth, 32).build(),
        )
        y += 44
        addValueRow(left, y, "screen.aemil.zoom.default", ZoomController.default(), -1.0, 64.0) {
            ZoomController.setLimits(newDefault = it)
        }
        y += 26
        addValueRow(left, y, "screen.aemil.zoom.minimum", ZoomController.minimum(), 1.0, ZoomController.maximum()) {
            ZoomController.setLimits(newMinimum = it)
        }
        y += 26
        addValueRow(left, y, "screen.aemil.zoom.maximum", ZoomController.maximum(), ZoomController.minimum(), 64.0) {
            ZoomController.setLimits(newMaximum = it)
        }
        y += 42
        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) { minecraft?.gui?.setScreen(controlsScreen) }
                .bounds(center - 100, y, 200, 20).build(),
        )
    }

    private fun addValueRow(
        x: Int,
        y: Int,
        labelKey: String,
        initial: Double,
        lowerBound: Double,
        upperBound: Double,
        update: (Double) -> Unit,
    ) {
        var value = initial
        fun label() = Component.translatable(labelKey, "${"%.1f".format(value)}×")
        addRenderableWidget(Button.builder(Component.literal("−")) {
            value = (value - 0.5).coerceIn(lowerBound, upperBound)
            update(value)
            clearWidgets()
            init()
        }.bounds(x, y, 28, 20).build())
        addRenderableWidget(Button.builder(label(), Button.OnPress {})
            .bounds(x + 32, y, 196, 20).build())
        addRenderableWidget(Button.builder(Component.literal("+")) {
            value = (value + 0.5).coerceIn(lowerBound, upperBound)
            update(value)
            clearWidgets()
            init()
        }.bounds(x + 232, y, 28, 20).build())
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)
    }
}
