package therealfarfetchd.illuminate.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.world.ClientWorld;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import therealfarfetchd.illuminate.client.MinecraftClientExt;
import therealfarfetchd.illuminate.client.api.Lights;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements MinecraftClientExt {

    @Shadow @Final @Mutable private Framebuffer framebuffer;

    @Override
    public void setFramebuffer(@NotNull Framebuffer fb) {
        this.framebuffer = fb;
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
    private void joinWorld(ClientWorld clientWorld, CallbackInfo ci) {
        Lights.getInstance().clear();
    }

}
