package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GLX.GL_DEPTH_ATTACHMENT
import com.mojang.blaze3d.platform.GLX.GL_FRAMEBUFFER
import com.mojang.blaze3d.platform.GLX.GL_RENDERBUFFER
import com.mojang.blaze3d.platform.GLX.glBindFramebuffer
import com.mojang.blaze3d.platform.GLX.glBindRenderbuffer
import com.mojang.blaze3d.platform.GLX.glFramebufferRenderbuffer
import com.mojang.blaze3d.platform.GLX.glGenFramebuffers
import com.mojang.blaze3d.platform.GLX.glGenRenderbuffers
import com.mojang.blaze3d.platform.GLX.glRenderbufferStorage
import net.minecraft.client.gl.GlFramebuffer
import org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24

class LightFramebuffer(width: Int, height: Int, clear: Boolean) : GlFramebuffer(width, height, true, clear) {

  override fun initFbo(width: Int, height: Int, clear: Boolean) {
    viewWidth = width
    viewHeight = height
    texWidth = width
    texHeight = height
    if (!GLX.isUsingFBOs()) {
      clear(clear)
    } else {
      fbo = glGenFramebuffers()
      // colorAttachment = TextureUtil.generateTextureId()
      if (useDepthAttachment) {
        depthAttachment = glGenRenderbuffers()
      }

      // setTexFilter(9728)
      // GlStateManager.bindTexture(colorAttachment)
      // texImage2D(3553, 0, 32856, texWidth, texHeight, 0, 6408, 5121, null as IntBuffer?)
      glBindFramebuffer(GL_FRAMEBUFFER, fbo)
      // GLX.glFramebufferTexture2D(GLX.GL_FRAMEBUFFER, GLX.GL_COLOR_ATTACHMENT0, 3553, colorAttachment, 0)
      if (useDepthAttachment) {
        glBindRenderbuffer(GL_RENDERBUFFER, depthAttachment)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, texWidth, texHeight)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthAttachment)
      }

      // glDrawBuffer(GL_NONE)

      checkFramebufferStatus()
      clear(clear)
      endRead()
    }
  }

}