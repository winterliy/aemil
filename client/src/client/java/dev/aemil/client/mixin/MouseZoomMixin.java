package dev.aemil.client.mixin;

import dev.aemil.client.zoom.ZoomController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseZoomMixin {
    @Inject(method = "onScroll(JDD)V", at = @At("HEAD"), cancellable = true)
    private void aemil$adjustZoom(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (vertical == 0.0 || Minecraft.getInstance().gui.screen() != null || !ZoomController.isHeld()) return;
        ZoomController.adjustFromScroll(vertical);
        ci.cancel();
    }
}
