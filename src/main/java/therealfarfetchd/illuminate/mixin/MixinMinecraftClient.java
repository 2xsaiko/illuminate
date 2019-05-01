package therealfarfetchd.illuminate.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.world.ClientWorld;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import therealfarfetchd.illuminate.client.MinecraftClientExt;
import therealfarfetchd.illuminate.client.api.Lights;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements MinecraftClientExt {

    @Shadow private GlFramebuffer framebuffer;

    @Override
    public void setFramebuffer(@NotNull GlFramebuffer fb) {
        this.framebuffer = fb;
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
    private void method_1481(ClientWorld clientWorld_1, CallbackInfo ci) {
        Lights.getInstance().clear();
    }

}
