package dev.aemil.client

import com.mojang.blaze3d.platform.InputConstants
import dev.aemil.client.screen.AemilSettingsScreen
import dev.aemil.client.zoom.ZoomController
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier

class AemilClient : ClientModInitializer {
    override fun onInitializeClient() {
        ZoomController.load()

        val category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("aemil", "client"))
        val zoomKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping("key.aemil.zoom", InputConstants.Type.KEYSYM, InputConstants.KEY_C, category),
        )
        val settingsKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping("key.aemil.settings", InputConstants.Type.KEYSYM, InputConstants.KEY_O, category),
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            ZoomController.updateKeyState(zoomKey.isDown)
            while (settingsKey.consumeClick()) {
                client.setScreen(AemilSettingsScreen(client.screen))
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.player == null) ZoomController.updateKeyState(false)
        }
    }
}
