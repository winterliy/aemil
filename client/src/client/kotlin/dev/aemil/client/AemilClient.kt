package dev.aemil.client

import com.mojang.blaze3d.platform.InputConstants
import dev.aemil.client.screen.ZoomControlsScreen
import dev.aemil.client.zoom.ZoomController
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import net.minecraft.network.chat.Component
import net.minecraft.client.gui.components.Button

class AemilClient : ClientModInitializer {
    override fun onInitializeClient() {
        ZoomController.load()

        val category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("aemil", "client"))
        val zoomKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping("key.aemil.zoom", InputConstants.Type.KEYBOARD, InputConstants.KEY_C, category),
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            ZoomController.updateKeyState(zoomKey.isDown)
            if (client.player == null) ZoomController.updateKeyState(false)
        }

        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            if (screen.title == Component.translatable("controls.title")) {
                Screens.getButtons(screen).add(
                    Button.builder(Component.translatable("screen.aemil.zoom.settings")) {
                        client.gui.setScreen(ZoomControlsScreen(screen))
                    }.bounds(8, scaledHeight - 28, 132, 20).build(),
                )
            }
        }
    }
}
