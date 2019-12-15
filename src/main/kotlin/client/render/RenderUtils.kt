package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.systems.RenderSystem.bindTexture
import com.mojang.blaze3d.systems.RenderSystem.color4f
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import org.lwjgl.opengl.GL11.GL_QUADS
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