package therealfarfetchd.illuminate.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer

interface MinecraftClientExt {

  fun setFramebuffer(fb: Framebuffer)

}

fun MinecraftClient.setFramebuffer(fb: Framebuffer) {
  (this as MinecraftClientExt).setFramebuffer(fb)
}