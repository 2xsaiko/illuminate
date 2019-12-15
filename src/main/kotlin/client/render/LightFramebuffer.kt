package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.platform.FramebufferInfo
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.Framebuffer

class LightFramebuffer(width: Int, height: Int, getError: Boolean) : Framebuffer(width, height, true, getError) {

  override fun initFbo(width: Int, height: Int, getError: Boolean) {
    RenderSystem.assertThread { RenderSystem.isOnRenderThreadOrInit() }
    viewportWidth = width
    viewportHeight = height
    textureWidth = width
    textureHeight = height
    fbo = GlStateManager.genFramebuffers()
    // colorAttachment = TextureUtil.generateTextureId()
    if (useDepthAttachment) {
      depthAttachment = GlStateManager.genRenderbuffers()
    }

    // setTexFilter(9728)
    // GlStateManager.bindTexture(colorAttachment)
    // GlStateManager.texImage2D(3553, 0, 32856, textureWidth, textureHeight, 0, 6408, 5121, null as IntBuffer?)
    GlStateManager.bindFramebuffer(FramebufferInfo.target, fbo)
    // GlStateManager.framebufferTexture2D(FramebufferInfo.target, FramebufferInfo.field_20459, 3553, colorAttachment, 0)
    if (useDepthAttachment) {
      GlStateManager.bindRenderbuffer(FramebufferInfo.renderBufferTarget, depthAttachment)
      GlStateManager.renderbufferStorage(FramebufferInfo.renderBufferTarget, 33190, textureWidth, textureHeight)
      GlStateManager.framebufferRenderbuffer(FramebufferInfo.target, FramebufferInfo.attachment, FramebufferInfo.renderBufferTarget, depthAttachment)
    }

    checkFramebufferStatus()
    this.clear(getError)
    endRead()
  }

}