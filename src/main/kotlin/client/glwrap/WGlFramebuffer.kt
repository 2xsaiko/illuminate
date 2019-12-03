package therealfarfetchd.illuminate.client.glwrap

import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30.glBindFramebuffer
import org.lwjgl.opengl.GL30.glDeleteFramebuffers
import org.lwjgl.opengl.GL30.glFramebufferTexture2D
import org.lwjgl.opengl.GL30.glGenFramebuffers

inline class WGlFramebuffer(val id: Int) {

  fun bind(target: Int = GL_FRAMEBUFFER) = glBindFramebuffer(target, id)

  fun attachTexture(attachment: Int, texture: WGlTexture2D, target:Int = GL_FRAMEBUFFER, textarget: Int = GL_TEXTURE_2D) {
    bind()
    glFramebufferTexture2D(target, attachment, textarget, texture.id, 0)
  }

  fun destroy() = glDeleteFramebuffers(id)

  companion object {
    fun create() = WGlFramebuffer(glGenFramebuffers())
  }

}