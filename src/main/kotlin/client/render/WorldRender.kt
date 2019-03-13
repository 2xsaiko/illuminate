package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.block.BlockRenderLayer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.CameraHelper
import net.minecraft.client.render.FrustumWithOrigin
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.GlMatrixFrustum
import net.minecraft.client.render.GuiLighting
import net.minecraft.client.render.Tessellator
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.math.MathHelper

// TODO: turn into Mixin of Lnet/minecraft/client/render/GameRenderer;renderCenter(FJ)V instead of copying the method
fun renderWorld(gr: GameRenderer, delta: Float, time: Long, i: Int) {
  val worldRenderer_1 = gr.client.worldRenderer
  val particleManager_1 = gr.client.particleManager
  val boolean_1 = false//gr.shouldRenderBlockOutline()
//  GlStateManager.enableCull()
  gr.client.profiler.swap("clearLights")
//  GlStateManager.viewport(0, 0, gr.client.window.framebufferWidth, gr.client.window.framebufferHeight)
//  gr.backgroundRenderer.renderBackground(delta)
  GlStateManager.clear(16640, MinecraftClient.IS_SYSTEM_MAC)
  gr.client.profiler.swap("camera")
//  gr.method_3185(delta)
  val frustum_1 = GlMatrixFrustum.get()
  CameraHelper.update(gr.client.player, gr.client.options.perspective == 2, gr.viewDistance, frustum_1)
  gr.client.profiler.swap("culling")
  val visibleRegion_1 = FrustumWithOrigin(frustum_1)
  val entity_1 = gr.client.getCameraEntity()!!
  val double_1 = MathHelper.lerp(delta.toDouble(), entity_1.prevRenderX, entity_1.x)
  val double_2 = MathHelper.lerp(delta.toDouble(), entity_1.prevRenderY, entity_1.y)
  val double_3 = MathHelper.lerp(delta.toDouble(), entity_1.prevRenderZ, entity_1.z)
  visibleRegion_1.setOrigin(double_1, double_2, double_3)
//  if (gr.client.options.viewDistance >= 4) {
//    gr.backgroundRenderer.applyFog(-1, delta)
//    gr.client.getProfiler().swap("sky")
//    GlStateManager.matrixMode(5889)
//    GlStateManager.loadIdentity()
//    GlStateManager.multMatrix(Matrix4f.method_4929(gr.method_3196(delta, true), gr.client.window.getFramebufferWidth().toFloat() / gr.client.window.getFramebufferHeight().toFloat(), 0.05f, gr.viewDistance * 2.0f))
//    GlStateManager.matrixMode(5888)
//    worldRenderer_1.renderSky(delta)
//    GlStateManager.matrixMode(5889)
//    GlStateManager.loadIdentity()
//    GlStateManager.multMatrix(Matrix4f.method_4929(gr.method_3196(delta, true), gr.client.window.getFramebufferWidth().toFloat() / gr.client.window.getFramebufferHeight().toFloat(), 0.05f, gr.viewDistance * MathHelper.SQUARE_ROOT_OF_TWO))
//    GlStateManager.matrixMode(5888)
//  }

//  gr.backgroundRenderer.applyFog(0, delta)
  GlStateManager.shadeModel(7425)
//  if (entity_1!!.y + entity_1!!.getEyeHeight().toDouble() < 128.0) {
//    gr.method_3206(worldRenderer_1, delta, double_1, double_2, double_3)
//  }

  gr.client.profiler.swap("prepareterrain")
//  gr.backgroundRenderer.applyFog(0, delta)
  gr.client.textureManager.bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
  GuiLighting.disable()
  gr.client.profiler.swap("terrain_setup")
//  gr.client.world.getChunkProvider().getLightingProvider().doLightUpdates(Integer.MAX_VALUE, true, true)
  worldRenderer_1.setUpTerrain(entity_1, delta, visibleRegion_1, i, gr.client.player.isSpectator)
//  gr.client.getProfiler().swap("updatechunks")
//  gr.client.worldRenderer.updateChunks(time)
  gr.client.profiler.swap("terrain")
  GlStateManager.matrixMode(5888)
  GlStateManager.pushMatrix()
  GlStateManager.disableAlphaTest()
  worldRenderer_1.renderLayer(BlockRenderLayer.SOLID, delta.toDouble(), entity_1)
  GlStateManager.enableAlphaTest()
  worldRenderer_1.renderLayer(BlockRenderLayer.MIPPED_CUTOUT, delta.toDouble(), entity_1)
  gr.client.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).pushFilter(false, false)
  worldRenderer_1.renderLayer(BlockRenderLayer.CUTOUT, delta.toDouble(), entity_1)
  gr.client.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).popFilter()
  GlStateManager.shadeModel(7424)
  GlStateManager.alphaFunc(516, 0.1f)
  GlStateManager.matrixMode(5888)
  GlStateManager.popMatrix()
  GlStateManager.pushMatrix()
  GuiLighting.enable()
  gr.client.profiler.swap("entities")
  worldRenderer_1.renderEntities(entity_1, visibleRegion_1, delta)
  GuiLighting.disable()
  gr.disableLightmap()
  GlStateManager.matrixMode(5888)
  GlStateManager.popMatrix()
  if (boolean_1 && gr.client.hitResult != null) {
    GlStateManager.disableAlphaTest()
    gr.client.profiler.swap("outline")
    worldRenderer_1.drawHighlightedBlockOutline(entity_1, gr.client.hitResult, 0, delta)
    GlStateManager.enableAlphaTest()
  }

  if (gr.client.debugRenderer.shouldRender()) {
    gr.client.debugRenderer.renderDebuggers(delta, time)
  }

  gr.client.profiler.swap("destroyProgress")
  GlStateManager.enableBlend()
  GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
  gr.client.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).pushFilter(false, false)
  worldRenderer_1.renderPartiallyBrokenBlocks(Tessellator.getInstance(), Tessellator.getInstance().bufferBuilder, entity_1, delta)
  gr.client.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).popFilter()
  GlStateManager.disableBlend()
  gr.enableLightmap()
//  gr.backgroundRenderer.applyFog(0, delta)
  gr.client.profiler.swap("particles")
  particleManager_1.renderUnlitParticles(entity_1, delta)
  gr.disableLightmap()
  GlStateManager.depthMask(false)
//  GlStateManager.enableCull()
  gr.client.profiler.swap("weather")
//  gr.method_3170(delta)
  GlStateManager.depthMask(true)
  worldRenderer_1.renderWorldBorder(entity_1, delta)
  GlStateManager.disableBlend()
//  GlStateManager.enableCull()
  GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
  GlStateManager.alphaFunc(516, 0.1f)
//  gr.backgroundRenderer.applyFog(0, delta)
  GlStateManager.enableBlend()
  GlStateManager.depthMask(false)
  gr.client.textureManager.bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
  GlStateManager.shadeModel(7425)
  gr.client.profiler.swap("translucent")
  worldRenderer_1.renderLayer(BlockRenderLayer.TRANSLUCENT, delta.toDouble(), entity_1)
  GlStateManager.shadeModel(7424)
  GlStateManager.depthMask(true)
//  GlStateManager.enableCull()
  GlStateManager.disableBlend()
  GlStateManager.disableFog()
//  if (entity_1!!.y + entity_1!!.getEyeHeight().toDouble() >= 128.0) {
//    gr.client.getProfiler().swap("aboveClouds")
//    gr.method_3206(worldRenderer_1, delta, double_1, double_2, double_3)
//  }

}