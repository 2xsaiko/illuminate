package therealfarfetchd.illuminate.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceManager;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import therealfarfetchd.illuminate.client.GameRendererExt;
import therealfarfetchd.illuminate.client.render.PostProcess;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements GameRendererExt {

    private PostProcess pp;

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void init(MinecraftClient mc, ResourceManager rm, CallbackInfo ci) {
        pp = new PostProcess(mc);
    }

    @Inject(method = "onResized(II)V", at = @At("RETURN"))
    private void resize(int width, int height, CallbackInfo ci) {
        pp.resize(width, height);
    }

    @Inject(method = "close()V", at = @At("RETURN"), remap = false)
    private void close(CallbackInfo ci) {
        pp.destroy();
    }

    @Inject(method = "renderCenter(FJ)V", at = @At(value = "HEAD"))
    private void renderLights(float delta, long nanoTime, CallbackInfo ci) {
        pp.setupLights(delta);
        pp.renderLightDepths(delta, nanoTime);
    }

    @Inject(method = "renderCenter(FJ)V", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 15))
    private void applyShader(float delta, long nanoTime, CallbackInfo ci) {
        pp.paintSurfaces(delta);
    }

    @NotNull
    @Override
    public PostProcess getPostProcess() {
        return pp;
    }

}
