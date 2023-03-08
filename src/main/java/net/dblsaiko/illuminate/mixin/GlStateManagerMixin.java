package net.dblsaiko.illuminate.mixin;

import com.mojang.blaze3d.platform.GlStateManager;

import org.lwjgl.opengl.GL31;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GlStateManager.class)
public abstract class GlStateManagerMixin {
    @ModifyArg(
            method = "<clinit>()V",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/IntStream;range(II)Ljava/util/stream/IntStream;"),
            index = 1
    )
    // This is needed because normally GlStateManager only allocates 12 states for active textures instead of the full 32.
    // Damn you, Mojang!
    private static int increaseTexturesBuffer(final int max) {
        return GL31.glGetInteger(GL31.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS) + 1;
    }
}
