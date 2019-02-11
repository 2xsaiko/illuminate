package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
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
  val buf = t.bufferBuilder
  GlStateManager.color4f(1f, 1f, 1f, 1f)

  for ((n, tex) in (listOf(mc.gameRenderer.postProcess.playerCamDepth.id) + mc.gameRenderer.postProcess.activeLights.map { it.depth.id }).withIndex()) {
    GlStateManager.bindTexture(tex)

    buf.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV)

    buf.vertex(width - (n + 1) * dist - (n + 1) * size, height - dist - size, 0.0).texture(0, 1).next()
    buf.vertex(width - (n + 1) * dist - (n + 1) * size, height - dist, 0.0).texture(0, 0).next()
    buf.vertex(width - (n + 1) * dist - n * size, height - dist, 0.0).texture(1, 0).next()
    buf.vertex(width - (n + 1) * dist - n * size, height - dist - size, 0.0).texture(1, 1).next()

    t.draw()
  }
}

fun mkShader(vshs: String, fshs: String): WGlShader {
  val vsh = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
  val fsh = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
  val shader = GL20.glCreateProgram()

  GL20.glShaderSource(vsh, vshs)
  GL20.glCompileShader(vsh)
  for (s in GL20.glGetShaderInfoLog(vsh, 65535).trimIndent().lineSequence()) Illuminate.Logger.warn(s)
  if (GL20.glGetShaderi(vsh, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE) {
    for ((i, l) in vshs.lineSequence().withIndex()) Illuminate.Logger.info("${i + 1}: $l")
    Illuminate.Logger.fatal("Failed to compile vertex shader")
    return WGlShader.None
  }

  GL20.glShaderSource(fsh, fshs)
  GL20.glCompileShader(fsh)
  for (s in GL20.glGetShaderInfoLog(fsh, 65535).trimIndent().lineSequence()) Illuminate.Logger.warn(s)
  if (GL20.glGetShaderi(fsh, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE) {
    for ((i, l) in fshs.lineSequence().withIndex()) Illuminate.Logger.info("${i + 1}: $l")
    Illuminate.Logger.fatal("Failed to compile fragment shader")
    return WGlShader.None
  }

  GL20.glAttachShader(shader, vsh)
  GL20.glAttachShader(shader, fsh)
  GL20.glLinkProgram(shader)

  for (s in GL20.glGetProgramInfoLog(shader, 65535).trimIndent().lineSequence()) Illuminate.Logger.warn(s)
  if (GL20.glGetProgrami(shader, GL20.GL_LINK_STATUS) != GL11.GL_TRUE) {
    Illuminate.Logger.fatal("Failed to link program")
    return WGlShader.None
  }

  GL20.glValidateProgram(shader)
  for (s in GL20.glGetProgramInfoLog(shader, 65535).trimIndent().lineSequence()) Illuminate.Logger.warn(s)
  if (GL20.glGetProgrami(shader, GL20.GL_VALIDATE_STATUS) != GL11.GL_TRUE) {
    Illuminate.Logger.fatal("Failed to validate program")
    return WGlShader.None
  }

  GL20.glDeleteShader(fsh)
  GL20.glDeleteShader(vsh)

  return WGlShader(shader)
}