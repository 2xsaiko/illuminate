package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.systems.RenderSystem.bindTexture
import com.mojang.blaze3d.systems.RenderSystem.color4f
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_TRUE
import org.lwjgl.opengl.GL20.GL_COMPILE_STATUS
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_LINK_STATUS
import org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.opengl.GL20.glAttachShader
import org.lwjgl.opengl.GL20.glCompileShader
import org.lwjgl.opengl.GL20.glCreateProgram
import org.lwjgl.opengl.GL20.glCreateShader
import org.lwjgl.opengl.GL20.glDeleteShader
import org.lwjgl.opengl.GL20.glGetProgramInfoLog
import org.lwjgl.opengl.GL20.glGetProgrami
import org.lwjgl.opengl.GL20.glGetShaderInfoLog
import org.lwjgl.opengl.GL20.glGetShaderi
import org.lwjgl.opengl.GL20.glLinkProgram
import org.lwjgl.opengl.GL20.glShaderSource
import org.lwjgl.opengl.GL20.glValidateProgram
import therealfarfetchd.illuminate.Illuminate
import therealfarfetchd.illuminate.client.glwrap.WGlShader
import therealfarfetchd.illuminate.client.postProcess

fun drawDebug() {
  val mc = MinecraftClient.getInstance()
  val width = mc.window.scaledWidth
  val height = mc.window.scaledHeight

  val size = 64.0
  val dist = 8.0

  val depth = mc.gameRenderer.postProcess.playerCamDepth
  depth.bind()

  val t = Tessellator.getInstance()
  val buf = t.buffer
  color4f(1f, 1f, 1f, 1f)

  for ((n, tex) in (listOf(mc.gameRenderer.postProcess.playerCamDepth.id) + mc.gameRenderer.postProcess.activeLights.map { it.depth.id }).withIndex()) {
    bindTexture(tex)

    buf.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE)

    buf.vertex(width - (n + 1) * dist - (n + 1) * size, height - dist - size, 0.0).texture(0f, 1f).next()
    buf.vertex(width - (n + 1) * dist - (n + 1) * size, height - dist, 0.0).texture(0f, 0f).next()
    buf.vertex(width - (n + 1) * dist - n * size, height - dist, 0.0).texture(1f, 0f).next()
    buf.vertex(width - (n + 1) * dist - n * size, height - dist - size, 0.0).texture(1f, 1f).next()

    t.draw()
  }
}

fun mkShader(vshs: String, fshs: String): WGlShader {
  val vsh = glCreateShader(GL_VERTEX_SHADER)
  val fsh = glCreateShader(GL_FRAGMENT_SHADER)
  val shader = glCreateProgram()

  glShaderSource(vsh, vshs)
  glCompileShader(vsh)
  for (s in glGetShaderInfoLog(vsh, 65535).trimIndent().lineSequence()) Illuminate.Logger.warn(s)
  if (glGetShaderi(vsh, GL_COMPILE_STATUS) != GL_TRUE) {
    for ((i, l) in vshs.lineSequence().withIndex()) Illuminate.Logger.info("${i + 1}: $l")
    Illuminate.Logger.fatal("Failed to compile vertex shader")
    return WGlShader.None
  }

  glShaderSource(fsh, fshs)
  glCompileShader(fsh)
  for (s in glGetShaderInfoLog(fsh, 65535).trimIndent().lineSequence()) Illuminate.Logger.warn(s)
  if (glGetShaderi(fsh, GL_COMPILE_STATUS) != GL_TRUE) {
    for ((i, l) in fshs.lineSequence().withIndex()) Illuminate.Logger.info("${i + 1}: $l")
    Illuminate.Logger.fatal("Failed to compile fragment shader")
    return WGlShader.None
  }

  glAttachShader(shader, vsh)
  glAttachShader(shader, fsh)
  glLinkProgram(shader)

  for (s in glGetProgramInfoLog(shader, 65535).trimIndent().lineSequence()) Illuminate.Logger.warn(s)
  if (glGetProgrami(shader, GL_LINK_STATUS) != GL_TRUE) {
    Illuminate.Logger.fatal("Failed to link program")
    return WGlShader.None
  }

  glValidateProgram(shader)
  for (s in glGetProgramInfoLog(shader, 65535).trimIndent().lineSequence()) Illuminate.Logger.warn(s)
  if (glGetProgrami(shader, GL_VALIDATE_STATUS) != GL_TRUE) {
    Illuminate.Logger.fatal("Failed to validate program")
    return WGlShader.None
  }

  glDeleteShader(fsh)
  glDeleteShader(vsh)

  return WGlShader(shader)
}