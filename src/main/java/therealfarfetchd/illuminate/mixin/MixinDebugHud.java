package therealfarfetchd.illuminate.mixin;

import net.dblsaiko.illuminate.client.DrawDebug;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At("RETURN"))
    private void render(MatrixStack mv, CallbackInfo ci) {
        DrawDebug.drawDebug(this.client, mv);
    }
}
