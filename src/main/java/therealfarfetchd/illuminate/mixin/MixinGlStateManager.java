package therealfarfetchd.illuminate.mixin;

import com.mojang.blaze3d.platform.GlStateManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(GlStateManager.class)
public abstract class MixinGlStateManager {

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyArg(
        method = "<clinit>()V",
        at = @At(value = "INVOKE", target = "Ljava/util/stream/IntStream;range(II)Ljava/util/stream/IntStream;"),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/GlStateManager;activeTexture:I"),
            to = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/GlStateManager;modelShadeMode:I")
        ),
        index = 1,
        remap = false
    )
    // This is needed because normally GlStateManager only allocates 8 states for active textures instead of the full 32.
    // Damn you, Mojang!
    private static int increaseTexturesBuffer(final int max) {
        return 32;
    }

}
