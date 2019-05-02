package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.block.BlockRenderLayer
import net.minecraft.client.render.Camera
import net.minecraft.client.render.FrustumWithOrigin
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.GlMatrixFrustum
import net.minecraft.client.render.GuiLighting
import net.minecraft.client.render.Tessellator
import net.minecraft.client.texture.SpriteAtlasTexture

// TODO: turn into Mixin of Lnet/minecraft/client/render/GameRenderer;renderCenter(FJ)V instead of copying the method
fun renderWorld(gr: GameRenderer, delta: Float, time: Long, i: Int) {
  val worldRenderer_1 = gr.client.worldRenderer
  val particleManager_1 = gr.client.particleManager
  val boolean_1 = false
//  GlStateManager.enableCull()
  gr.client.getProfiler().swap("camera")
//  gr.applyCameraTransformations(delta)
  val camera_1 = Camera() // gr.camera
  GlStateManager.pushMatrix()
  camera_1.update(gr.client.world, (if (gr.client.getCameraEntity() == null) gr.client.player else gr.client.getCameraEntity()), false, false, delta)
  GlStateManager.popMatrix()
  val frustum_1 = GlMatrixFrustum.get()
  gr.client.getProfiler().swap("clear")
//  GlStateManager.viewport(0, 0, gr.client.window.getFramebufferWidth(), gr.client.window.getFramebufferHeight())
//  gr.backgroundRenderer.renderBackground(camera_1, delta)
//  GlStateManager.clear(16640, MinecraftClient.IS_SYSTEM_MAC)
  gr.client.getProfiler().swap("culling")
  val visibleRegion_1 = FrustumWithOrigin(frustum_1)
  val double_1 = camera_1.getPos().x
  val double_2 = camera_1.getPos().y
  val double_3 = camera_1.getPos().z
  visibleRegion_1.setOrigin(double_1, double_2, double_3)
//  if (gr.client.options.viewDistance >= 4) {
//    gr.backgroundRenderer.applyFog(camera_1, -1)
//    gr.client.getProfiler().swap("sky")
//    GlStateManager.matrixMode(5889)
//    GlStateManager.loadIdentity()
//    GlStateManager.multMatrix(Matrix4f.method_4929(gr.getFov(camera_1, delta, true), gr.client.window.getFramebufferWidth().toFloat() / gr.client.window.getFramebufferHeight().toFloat(), 0.05f, gr.viewDistance * 2.0f))
//    GlStateManager.matrixMode(5888)
//    worldRenderer_1.renderSky(delta)
//    GlStateManager.matrixMode(5889)
//    GlStateManager.loadIdentity()
//    GlStateManager.multMatrix(Matrix4f.method_4929(gr.getFov(camera_1, delta, true), gr.client.window.getFramebufferWidth().toFloat() / gr.client.window.getFramebufferHeight().toFloat(), 0.05f, gr.viewDistance * MathHelper.SQUARE_ROOT_OF_TWO))
//    GlStateManager.matrixMode(5888)
//  }

//  gr.backgroundRenderer.applyFog(camera_1, 0)
  GlStateManager.shadeModel(7425)
//  if (camera_1.getPos().y < 128.0) {
//    gr.renderAboveClouds(camera_1, worldRenderer_1, delta, double_1, double_2, double_3)
//  }

  gr.client.getProfiler().swap("prepareterrain")
//  gr.backgroundRenderer.applyFog(camera_1, 0)
  gr.client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
  GuiLighting.disable()
  gr.client.getProfiler().swap("terrain_setup")
//  gr.client.world.method_2935().getLightingProvider().doLightUpdates(Integer.MAX_VALUE, true, true)
  worldRenderer_1.setUpTerrain(camera_1, visibleRegion_1, i, gr.client.player.isSpectator())
//  gr.client.getProfiler().swap("updatechunks")
//  gr.client.worldRenderer.updateChunks(long_1)
  gr.client.getProfiler().swap("terrain")
  GlStateManager.matrixMode(5888)
  GlStateManager.pushMatrix()
  GlStateManager.disableAlphaTest()
  worldRenderer_1.renderLayer(BlockRenderLayer.SOLID, camera_1)
  GlStateManager.enableAlphaTest()
  worldRenderer_1.renderLayer(BlockRenderLayer.MIPPED_CUTOUT, camera_1)
  gr.client.getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).pushFilter(false, false)
  worldRenderer_1.renderLayer(BlockRenderLayer.CUTOUT, camera_1)
  gr.client.getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).popFilter()
  GlStateManager.shadeModel(7424)
  GlStateManager.alphaFunc(516, 0.1f)
  GlStateManager.matrixMode(5888)
  GlStateManager.popMatrix()
  GlStateManager.pushMatrix()
  GuiLighting.enable()
  gr.client.getProfiler().swap("entities")
  worldRenderer_1.renderEntities(camera_1, visibleRegion_1, delta)
  GuiLighting.disable()
  gr.disableLightmap()
  GlStateManager.matrixMode(5888)
  GlStateManager.popMatrix()
  if (boolean_1 && gr.client.hitResult != null) {
    GlStateManager.disableAlphaTest()
    gr.client.getProfiler().swap("outline")
    worldRenderer_1.drawHighlightedBlockOutline(camera_1, gr.client.hitResult, 0)
    GlStateManager.enableAlphaTest()
  }

  if (gr.client.debugRenderer.shouldRender()) {
    gr.client.debugRenderer.renderDebuggers(time)
  }

  gr.client.getProfiler().swap("destroyProgress")
  GlStateManager.enableBlend()
  GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
  gr.client.getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).pushFilter(false, false)
  worldRenderer_1.renderPartiallyBrokenBlocks(Tessellator.getInstance(), Tessellator.getInstance().bufferBuilder, camera_1)
  gr.client.getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).popFilter()
  GlStateManager.disableBlend()
  gr.enableLightmap()
//  gr.backgroundRenderer.applyFog(camera_1, 0)
  gr.client.getProfiler().swap("particles")
  particleManager_1.renderParticles(camera_1, delta)
  gr.disableLightmap()
  GlStateManager.depthMask(false)
//  GlStateManager.enableCull()
  gr.client.getProfiler().swap("weather")
//  gr.renderWeather(delta)
  GlStateManager.depthMask(true)
  worldRenderer_1.renderWorldBorder(camera_1, delta)
  GlStateManager.disableBlend()
//  GlStateManager.enableCull()
  GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
  GlStateManager.alphaFunc(516, 0.1f)
//  gr.backgroundRenderer.applyFog(camera_1, 0)
  GlStateManager.enableBlend()
  GlStateManager.depthMask(false)
  gr.client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
  GlStateManager.shadeModel(7425)
  gr.client.getProfiler().swap("translucent")
  worldRenderer_1.renderLayer(BlockRenderLayer.TRANSLUCENT, camera_1)
  GlStateManager.shadeModel(7424)
  GlStateManager.depthMask(true)
//  GlStateManager.enableCull()
  GlStateManager.disableBlend()
  GlStateManager.disableFog()
//  if (camera_1.getPos().y >= 128.0) {
//    gr.client.getProfiler().swap("aboveClouds")
//    gr.renderAboveClouds(camera_1, worldRenderer_1, delta, double_1, double_2, double_3)
//  }

//  gr.client.getProfiler().swap("hand")
//  if (gr.renderHand) {
//    GlStateManager.clear(256, MinecraftClient.IS_SYSTEM_MAC)
//    gr.renderHand(camera_1, delta)
//  }

}