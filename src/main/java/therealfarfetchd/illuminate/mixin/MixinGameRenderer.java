package therealfarfetchd.illuminate.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Quaternion;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import therealfarfetchd.illuminate.client.GameRendererExt;
import therealfarfetchd.illuminate.client.Matrix4fExtKt;
import therealfarfetchd.illuminate.client.api.Light;
import therealfarfetchd.illuminate.client.render.PostProcess;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements GameRendererExt {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private Camera camera;

    private PostProcess pp;

    private Light activeRenderLight = null;

    private float lastTickDelta;

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/render/BufferBuilderStorage;)V", at = @At("RETURN"))
    private void init(MinecraftClient client, ResourceManager resourceManager, BufferBuilderStorage bufferBuilderStorage, CallbackInfo ci) {
        pp = new PostProcess(client);
    }

    @Inject(method = "onResized(II)V", at = @At("RETURN"))
    private void resize(int width, int height, CallbackInfo ci) {
        pp.resize(width, height);
    }

    @Inject(method = "close()V", at = @At("RETURN"), remap = false)
    private void close(CallbackInfo ci) {
        pp.destroy();
    }

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "HEAD"))
    private void renderLights(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        if (activeRenderLight != null) return;
        pp.setupLights(tickDelta);
        pp.renderLightDepths(tickDelta, limitTime);
    }

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/util/math/Matrix4f;)V"))
    private void applyShader(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        if (activeRenderLight != null) return;
        pp.paintSurfaces(tickDelta);
    }

    // rendering fixes for light perspective

    @Inject(method = "updateTargetedEntity", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntity(float tickDelta, CallbackInfo ci) {
        if (activeRenderLight != null) ci.cancel();
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void saveTickDelta(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        lastTickDelta = tickDelta;
    }

    @ModifyVariable(method = "renderWorld", at = @At(value = "HEAD", ordinal = 0))
    private Camera getCamera(Camera cam) {
        if (activeRenderLight != null) return camera;

        Camera camera = new Camera();
        camera.update(client.world, client.getCameraEntity() == null ? client.player : client.getCameraEntity(), false, false, lastTickDelta);
        return camera;
    }

    @Inject(method = "method_22973", at = @At("HEAD"), cancellable = true)
    private void getProjectionMatrix(Camera camera, float f, boolean bl, CallbackInfoReturnable<Matrix4f> cir) {
        if (activeRenderLight == null) return;

        Matrix4f matrix4f = new Matrix4f();
        Matrix4fExtKt.set(matrix4f, pp.getLights().get(activeRenderLight).getP());

        cir.setReturnValue(matrix4f);
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobView(MatrixStack matrixStack, float f, CallbackInfo ci) {
        if (activeRenderLight != null) ci.cancel();
    }

    @Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void bobViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo ci) {
        if (activeRenderLight != null) ci.cancel();
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(MatrixStack matrixStack, Camera camera, float f, CallbackInfo ci) {
        if (activeRenderLight != null) ci.cancel();
    }

    @ModifyVariable(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V", ordinal = 0))
    private float getNauseaStrength(float f) {
        if (activeRenderLight == null) return f;

        return 0f;
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V"))
    private void multiply(MatrixStack matrixStack, Quaternion quaternion) {
        if (activeRenderLight != null) return;

        matrixStack.multiply(quaternion);
    }

    @NotNull
    @Override
    public PostProcess getPostProcess() {
        return pp;
    }

    @NotNull
    @Override
    public Light getActiveRenderLight() {
        return activeRenderLight;
    }

    @Override
    public void setActiveRenderLight(@NotNull Light activeRenderLight) {
        this.activeRenderLight = activeRenderLight;
    }


}
