package therealfarfetchd.illuminate.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dblsaiko.illuminate.client.GameRendererExt;
import net.dblsaiko.illuminate.client.LightContainer;
import net.dblsaiko.illuminate.client.render.PostProcess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements GameRendererExt {
    @Shadow
    @Final
    MinecraftClient client;
    @Shadow
    @Final
    private Camera camera;

    @Unique
    private PostProcess pp;

    @Unique
    private LightContainer activeRenderLight = null;

    @Unique
    private float lastTickDelta;

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/render/item/HeldItemRenderer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/render/BufferBuilderStorage;)V", at = @At("RETURN"))
    private void init(MinecraftClient client, HeldItemRenderer heldItemRenderer, ResourceManager resourceManager, BufferBuilderStorage buffers, CallbackInfo ci) {
        this.pp = new PostProcess(client);
    }

    @Inject(method = "onResized(II)V", at = @At("RETURN"))
    private void resize(int width, int height, CallbackInfo ci) {
        this.pp.resize(width, height);
    }

    @Inject(method = "close()V", at = @At("RETURN"), remap = false)
    private void close(CallbackInfo ci) {
        this.pp.destroy();
    }

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "HEAD"))
    private void renderLights(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        if (this.activeRenderLight != null) return;
        this.pp.setupLights(tickDelta);
        this.pp.renderLightDepths(tickDelta, limitTime, matrix);
    }

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void applyShader(float tickDelta, long limitTime, MatrixStack modelview, CallbackInfo ci, boolean bl, Camera camera, MatrixStack projection, double d, Matrix4f matrix4f, Matrix3f matrix3f) {
        if (this.activeRenderLight != null) return;
        this.pp.paintSurfaces(tickDelta, modelview, projection);
    }

    // rendering fixes for light perspective

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At("HEAD"))
    private void saveTickDelta(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        this.lastTickDelta = tickDelta;
    }

    @Inject(method = "updateTargetedEntity(F)V", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntity(float tickDelta, CallbackInfo ci) {
        if (this.activeRenderLight != null) ci.cancel();
    }

    @ModifyVariable(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "STORE"), ordinal = 0, name = "camera")
    private Camera getCamera(Camera cam) {
        if (this.activeRenderLight == null) return cam;

        Camera camera = new Camera();
        camera.update(this.client.world, this.client.getCameraEntity() == null ? this.client.player : this.client.getCameraEntity(), false, false, this.lastTickDelta);
        return camera;
    }

    @Inject(method = "getBasicProjectionMatrix(D)Lorg/joml/Matrix4f;", at = @At("HEAD"), cancellable = true)
    private void getProjectionMatrix(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (this.activeRenderLight == null) return;

        Matrix4f matrix4f = new Matrix4f(this.activeRenderLight.p());

        cir.setReturnValue(matrix4f);
    }

    @Inject(method = "bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("HEAD"), cancellable = true)
    private void bobView(MatrixStack matrixStack, float f, CallbackInfo ci) {
        if (this.activeRenderLight != null) ci.cancel();
    }

    @Inject(method = "bobViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("HEAD"), cancellable = true)
    private void bobViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo ci) {
        if (this.activeRenderLight != null) ci.cancel();
    }

    @Inject(method = "renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", at = @At("HEAD"), cancellable = true)
    private void renderHand(MatrixStack matrixStack, Camera camera, float f, CallbackInfo ci) {
        if (this.activeRenderLight != null) ci.cancel();
    }

    @Redirect(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0))
    private float getNauseaStrength(float delta, float first, float second) {
        return this.activeRenderLight == null ? MathHelper.lerp(delta, first, second) : 0f;
    }

//    @Redirect(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V"))
//    private void multiply(MatrixStack matrixStack, Quaternionf quaternion) {
//        if (this.activeRenderLight != null) return;
//
//        matrixStack.multiply(quaternion);
//    }

    @Redirect(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"))
    private void clear(int i, boolean bl) {
        if (this.activeRenderLight != null) return;

        RenderSystem.clear(i, bl);
    }

    @NotNull
    @Override
    public PostProcess postProcess() {
        return this.pp;
    }

    @Override
    public LightContainer activeRenderLight() {
        return this.activeRenderLight;
    }

    @Override
    public void setActiveRenderLight(LightContainer activeRenderLight) {
        this.activeRenderLight = activeRenderLight;
    }
}
