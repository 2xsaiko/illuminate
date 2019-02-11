package therealfarfetchd.illuminate.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.GlFramebuffer

interface MinecraftClientExt {

  fun setFramebuffer(fb: GlFramebuffer)

}

fun MinecraftClient.setFramebuffer(fb: GlFramebuffer) {
  (this as MinecraftClientExt).setFramebuffer(fb)
}