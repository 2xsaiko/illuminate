package therealfarfetchd.illuminate.client.glwrap

import com.mojang.blaze3d.platform.GLX.glBindFramebuffer
import com.mojang.blaze3d.platform.GLX.glDeleteFramebuffers
import com.mojang.blaze3d.platform.GLX.glFramebufferTexture2D
import com.mojang.blaze3d.platform.GLX.glGenFramebuffers
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER

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