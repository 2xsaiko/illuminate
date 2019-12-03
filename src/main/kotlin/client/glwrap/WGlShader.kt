package therealfarfetchd.illuminate.client.glwrap

import org.lwjgl.opengl.GL20.glDeleteProgram
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glUseProgram


inline class WGlShader(val id: Int) {

  val isValid
    get() = id > 0

  fun enable() {
    if (isValid) glUseProgram(id)
  }

  fun disable() {
    if (isValid) glUseProgram(0)
  }

  fun destroy() {
    if (isValid) glDeleteProgram(id)
  }

  fun getUniformLocation(name: String): Int {
    return glGetUniformLocation(id, name)
  }

  companion object {
    val None = WGlShader(0)
  }

}