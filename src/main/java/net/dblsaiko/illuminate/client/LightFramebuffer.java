package net.dblsaiko.illuminate.client;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.SimpleFramebuffer;

public class LightFramebuffer extends SimpleFramebuffer {
    public LightFramebuffer(int width, int height, boolean getError) {
        super(width, height, true, getError);
    }

    @Override
    public void initFbo(int width, int height, boolean getError) {
        super.initFbo(width, height, getError);

        // Get rid of color texture since we don't need it :^)
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.fbo);
        GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, 0, 0);
        GlStateManager._deleteTexture(this.colorAttachment);
        this.colorAttachment = 0;

        this.checkFramebufferStatus();
    }
}
