package therealfarfetchd.illuminate.mixin;

import net.minecraft.client.gui.hud.DebugHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import therealfarfetchd.illuminate.client.render.RenderUtilsKt;

@Mixin(DebugHud.class)
public abstract class MixinDebugHud {

    @Inject(method = "draw()V", at = @At("RETURN"))
    private void draw(CallbackInfo ci) {
        RenderUtilsKt.drawDebug();
    }

}
