package therealfarfetchd.illuminate.client.glwrap

import com.mojang.blaze3d.platform.GLX.glDeleteProgram
import com.mojang.blaze3d.platform.GLX.glGetUniformLocation
import com.mojang.blaze3d.platform.GLX.glUseProgram

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