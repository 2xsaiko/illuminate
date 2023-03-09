package net.dblsaiko.illuminate.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL30;

import java.nio.IntBuffer;

public class HdrFramebuffer extends SimpleFramebuffer
{
    public HdrFramebuffer(int width, int height, boolean useDepth, boolean getError)
    {
        super(width, height, useDepth, getError);
    }

    public void initFbo(int width, int height, boolean getError)
    {
        super.initFbo(width, height, getError);

        GlStateManager._bindTexture(this.colorAttachment);
        GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, this.textureWidth, this.textureHeight, 0, GlConst.GL_RGBA, GlConst.GL_FLOAT, (IntBuffer)null);
        this.endRead();
    }
}
