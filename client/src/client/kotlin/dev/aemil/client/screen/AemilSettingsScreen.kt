package dev.aemil.client.screen

import dev.aemil.client.zoom.ZoomController
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class AemilSettingsScreen(private val parent: Screen?) : Screen(Component.translatable("screen.aemil.settings")) {
    override fun init() {
        val center = width / 2
        val rowWidth = 260
        val left = center - rowWidth / 2
        val row = 26
        var y = height / 4

        addRenderableWidget(Button.builder(Component.translatable("screen.aemil.zoom.instructions"), Button.OnPress {})
            .bounds(left, y, rowWidth, 32).build())
        y += 44
        addValueRow(left, y, "screen.aemil.zoom.default", ZoomController.default(), -1.0, 32.0) {
            ZoomController.setLimits(newDefault = it)
        }
        y += row + 4
        addValueRow(left, y, "screen.aemil.zoom.minimum", ZoomController.minimum(), 1.0, ZoomController.maximum()) {
            ZoomController.setLimits(newMinimum = it)
        }
        y += row + 4
        addValueRow(left, y, "screen.aemil.zoom.maximum", ZoomController.maximum(), ZoomController.minimum(), 64.0) {
            ZoomController.setLimits(newMaximum = it)
        }
        y += row + 18
        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) { onClose() }
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
        fun buttonText() = Component.translatable(labelKey, "${"%.1f".format(value)}×")
        addRenderableWidget(Button.builder(Component.literal("−")) {
            value = (value - 0.5).coerceIn(lowerBound, upperBound)
            update(value)
            clearWidgets()
            init()
        }.bounds(x, y, 28, 20).build())
        addRenderableWidget(Button.builder(buttonText(), Button.OnPress {})
            .bounds(x + 32, y, 196, 20).build())
        addRenderableWidget(Button.builder(Component.literal("+")) {
            value = (value + 0.5).coerceIn(lowerBound, upperBound)
            update(value)
            clearWidgets()
            init()
        }.bounds(x + 232, y, 28, 20).build())
    }

    override fun render(context: net.minecraft.client.gui.GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }
}
