package dev.aemil.client.mixin;

import dev.aemil.client.zoom.ZoomController;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraZoomMixin {
    @Inject(method = "calculateFov(F)F", at = @At("RETURN"), cancellable = true)
    private void aemil$applyZoom(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(ZoomController.applyToFov(cir.getReturnValue()));
    }
}
