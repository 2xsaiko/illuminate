package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer

class LightFramebuffer(width: Int, height: Int, getError: Boolean) : SimpleFramebuffer(width, height, true, getError) {

  override fun initFbo(width: Int, height: Int, getError: Boolean) {
    RenderSystem.assertOnRenderThreadOrInit()
    viewportWidth = width
    viewportHeight = height
    textureWidth = width
    textureHeight = height
    fbo = GlStateManager.glGenFramebuffers()
    // colorAttachment = TextureUtil.generateTextureId()
    if (useDepthAttachment) {
      depthAttachment = GlStateManager.glGenRenderbuffers()
    }

    // setTexFilter(9728)
    // GlStateManager.bindTexture(colorAttachment)
    // GlStateManager.texImage2D(3553, 0, 32856, textureWidth, textureHeight, 0, 6408, 5121, null as IntBuffer?)
    GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fbo)
    // GlStateManager.framebufferTexture2D(FramebufferInfo.target, FramebufferInfo.field_20459, 3553, colorAttachment, 0)
    if (useDepthAttachment) {
      GlStateManager._glBindRenderbuffer(GlConst.GL_RENDERBUFFER, depthAttachment)
      GlStateManager._glRenderbufferStorage(GlConst.GL_RENDERBUFFER, GlConst.GL_DEPTH_COMPONENT24, textureWidth, textureHeight)
      GlStateManager._glFramebufferRenderbuffer(GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_RENDERBUFFER, depthAttachment)
    }

    checkFramebufferStatus()
    this.clear(getError)
    endRead()
  }

}
