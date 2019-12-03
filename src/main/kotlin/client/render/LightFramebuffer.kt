package therealfarfetchd.illuminate.client.render

import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24
import org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30.GL_RENDERBUFFER
import org.lwjgl.opengl.GL30.glBindFramebuffer
import org.lwjgl.opengl.GL30.glBindRenderbuffer
import org.lwjgl.opengl.GL30.glFramebufferRenderbuffer
import org.lwjgl.opengl.GL30.glGenFramebuffers
import org.lwjgl.opengl.GL30.glGenRenderbuffers
import org.lwjgl.opengl.GL30.glRenderbufferStorage

class LightFramebuffer(width: Int, height: Int, clear: Boolean) : Framebuffer(width, height, true, clear) {

  override fun initFbo(width: Int, height: Int, clear: Boolean) {
    viewportWidth = width
    viewportHeight = height
    textureWidth = width
    textureHeight = height
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
      glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, textureWidth, textureHeight)
      glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthAttachment)
    }

    // glDrawBuffer(GL_NONE)

    checkFramebufferStatus()
    clear(clear)
    endRead()
  }

}