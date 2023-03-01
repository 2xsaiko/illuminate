package therealfarfetchd.illuminate.client

import net.dblsaiko.illuminate.client.MinecraftClientExt
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer

fun MinecraftClient.setFramebuffer(fb: Framebuffer) {
  MinecraftClientExt.from(this).setFramebuffer(fb)
}