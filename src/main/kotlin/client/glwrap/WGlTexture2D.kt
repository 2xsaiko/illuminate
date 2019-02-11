package therealfarfetchd.illuminate.client.glwrap

import com.mojang.blaze3d.platform.GlStateManager.bindTexture
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glDeleteTextures
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL11.glTexParameteri

inline class WGlTexture2D(val id: Int) {

  fun destroy() = glDeleteTextures(id)

  fun bind() = bindTexture(id)

  fun unbind() = bindTexture(0)

  fun texImage(internalFormat: Int, width: Int, height: Int, format: Int, type: Int, pixels: IntArray) {
    bind()
    glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, pixels)
  }

  fun texImage(internalFormat: Int, width: Int, height: Int, format: Int, type: Int, pixels: FloatArray) {
    bind()
    glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, pixels)
  }

  fun texImage(internalFormat: Int, width: Int, height: Int, format: Int, type: Int, ptr: Long) {
    bind()
    glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, ptr)
  }

  fun texParameter(pname: Int, param: Int) {
    bind()
    glTexParameteri(GL_TEXTURE_2D, pname, param)
  }

  companion object {
    fun create(): WGlTexture2D = WGlTexture2D(glGenTextures())
  }

}