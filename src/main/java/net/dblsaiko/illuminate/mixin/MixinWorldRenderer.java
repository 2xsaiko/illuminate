package net.dblsaiko.illuminate.mixin;

import net.dblsaiko.illuminate.client.GameRendererExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "canDrawEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void canDrawEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if (GameRendererExt.from(this.client.gameRenderer).activeRenderLight() == null) {
            return;
        }

        cir.setReturnValue(false);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 3))
    private Entity getFocusedEntity(Camera camera) {
        if (GameRendererExt.from(this.client.gameRenderer).activeRenderLight() == null) {
            return camera.getFocusedEntity();
        }

        return this.client.player;
    }
}
