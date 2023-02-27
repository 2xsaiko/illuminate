package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.systems.RenderSystem.activeTexture
import com.mojang.blaze3d.systems.RenderSystem.bindTexture
import com.mojang.blaze3d.systems.RenderSystem.clear
import com.mojang.blaze3d.systems.RenderSystem.colorMask
import com.mojang.blaze3d.systems.RenderSystem.depthMask
import com.mojang.blaze3d.systems.RenderSystem.disableCull
import com.mojang.blaze3d.systems.RenderSystem.disableTexture
import com.mojang.blaze3d.systems.RenderSystem.enableTexture
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.option.Perspective
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_NEAREST
import org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.opengl.GL11.glDrawBuffer
import org.lwjgl.opengl.GL11.glMatrixMode
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.GL_TEXTURE2
import org.lwjgl.opengl.GL13.GL_TEXTURE3
import org.lwjgl.opengl.GL13.GL_TEXTURE4
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL20.glUniform1i
import org.lwjgl.opengl.GL20.glUniform3f
import org.lwjgl.opengl.GL20.glUniformMatrix4fv
import org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT
import org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER
import org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER
import org.lwjgl.opengl.GL30.glBindFramebuffer
import org.lwjgl.opengl.GL30.glBlitFramebuffer
import therealfarfetchd.illuminate.client.activeRenderLight
import therealfarfetchd.illuminate.client.api.Light
import therealfarfetchd.illuminate.client.glwrap.WGlFramebuffer
import therealfarfetchd.illuminate.client.glwrap.WGlShader
import therealfarfetchd.illuminate.client.glwrap.WGlTexture2D
import therealfarfetchd.illuminate.client.init.Shaders
import therealfarfetchd.illuminate.client.setFramebuffer
import therealfarfetchd.illuminate.common.util.ext.ortho

private val matBuf = BufferUtils.createFloatBuffer(16)

@Suppress("PrivatePropertyName")
class PostProcess(private val mc: MinecraftClient) {

  private val target = mc.framebuffer

  private val width = target.viewportWidth
  private val height = target.viewportHeight

  private var uMvp = 0
  private var uCamInv = 0
  private var uWidth = 0
  private var uHeight = 0
  private var uWorld = 0
  private var uDepth = 0
  private val uLightTex = IntArray(10)
  private val uLightDepth = IntArray(10)
  private val uLightCam = IntArray(10)
  private val uLightPos = IntArray(10)
  private var uLightCount = 0

  private val offscreenFb = SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC)
  val lightDepthFb = LightFramebuffer(1024, 1024, MinecraftClient.IS_SYSTEM_MAC)
  private val blitFb = WGlFramebuffer.create()

  val playerCamDepth = WGlTexture2D.create()

  val lights: MutableMap<Light, LightContainer> = HashMap()

  var activeLights: Set<LightContainer> = emptySet()
    private set

  init {
    playerCamDepth.texParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    playerCamDepth.texParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    playerCamDepth.texParameter(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    playerCamDepth.texParameter(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

    blitFb.bind()
    glDrawBuffer(GL11.GL_NONE)
    mc.framebuffer.beginWrite(false)
  }

  fun onShaderReload() {
    val shader = WGlShader(Shaders.lighting())

    if (shader.isValid) {
      shader.enable()

      uMvp = shader.getUniformLocation("mvp")
      uCamInv = shader.getUniformLocation("camInv")
      uWidth = shader.getUniformLocation("width")
      uHeight = shader.getUniformLocation("height")
      uWorld = shader.getUniformLocation("world")
      uDepth = shader.getUniformLocation("depth")

      repeat(10) {
        uLightTex[it] = shader.getUniformLocation("lightTex[$it]")
        uLightDepth[it] = shader.getUniformLocation("lightDepth[$it]")
        uLightCam[it] = shader.getUniformLocation("lightCam[$it]")
        uLightPos[it] = shader.getUniformLocation("lightPos[$it]")
      }

      uLightCount = shader.getUniformLocation("lightCount")

      shader.disable()
    }
  }

  /**
   * Determine which lights should be rendered from the player's position
   */
  fun setupLights(delta: Float) {
    activeLights =
      if (lights.size < 10) lights.values.toSet()
      else {
        val camPos = Vector3f(mc.gameRenderer.camera.pos.x.toFloat(), mc.gameRenderer.camera.pos.y.toFloat(), mc.gameRenderer.camera.pos.z.toFloat())
        lights.values.asSequence().sortedBy { (it.light.pos.sub(camPos)).lengthSquared() }.take(10).toSet()
      }
  }

  /**
   * Resize the framebuffers and textures to the specified size.
   */
  fun resize(width: Int, height: Int) {
    offscreenFb.resize(width, height, MinecraftClient.IS_SYSTEM_MAC)
  }

  /**
   * Draws the world from each light's perspective and saves the depth buffer for later use.
   */
  fun renderLightDepths(delta: Float, nanoTime: Long, matrix: MatrixStack) {
    if (activeLights.isEmpty()) return

    val oldHudHidden = mc.options.hudHidden
    val oldCam = mc.cameraEntity
    val oldPerspective = mc.options.perspective
    mc.options.hudHidden = true
    mc.options.perspective = Perspective.FIRST_PERSON
    val window = mc.framebuffer
    for (lc in activeLights) {
      lightDepthFb.beginWrite(true)
      glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
      mc.setFramebuffer(lightDepthFb)

      matrix.push()

      lc.light.prepare(delta)

      // setupCamera(lc)

      val lightSource = LightSource(mc.world!!, lc.light)
      mc.cameraEntity = lightSource
      disableCull()

      try {
        mc.gameRenderer.activeRenderLight = lc
        mc.gameRenderer.renderWorld(delta, nanoTime, MatrixStack())
      } finally {
        mc.gameRenderer.activeRenderLight = null
      }

      blitDepthToTex(lightDepthFb, lc.depth)

      matrix.pop()
    }

    mc.setFramebuffer(window)
    window.beginWrite(true)

    mc.cameraEntity = oldCam
    mc.options.hudHidden = oldHudHidden
    mc.options.perspective = oldPerspective
  }

  /**
   * Applies the shader onto the target framebuffer
   */
  fun paintSurfaces(delta: Float, modelview: MatrixStack, projection: MatrixStack) {
    val shader = WGlShader(Shaders.lighting())

    if (activeLights.isEmpty()) return
    if (!shader.isValid) return // something is fucked

    val camera = mc.gameRenderer.camera
    val cameraPosVec = camera.pos

    blitDepthToTex(target, playerCamDepth)

    clear(GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC)

    modelview.push()
    modelview.translate(-cameraPosVec.x, -cameraPosVec.y, -cameraPosVec.z)
    paintSurfaces(target, offscreenFb, modelview.peek().positionMatrix, projection.peek().positionMatrix)
    modelview.pop()
    blit(offscreenFb, target)

    target.beginWrite(true)
  }

  /**
   * Apply the shader onto the source framebuffer and draw into the target framebuffer
   */
  private fun paintSurfaces(from: Framebuffer, into: Framebuffer, modelview: Matrix4f, projection: Matrix4f) {
    val shader = WGlShader(Shaders.lighting())

    val sourceW = from.textureWidth
    val sourceH = from.textureHeight
    val targetH = into.textureHeight
    val targetW = into.textureWidth

    from.endWrite()

    shader.enable()
    glUniform1i(uWidth, sourceW)
    glUniform1i(uHeight, sourceH)

    enableTexture()
    from.beginRead()
    glUniform1i(uWorld, 0)

    activeTexture(GL_TEXTURE2)
    enableTexture()
    playerCamDepth.bind()
    glUniform1i(uDepth, 2)
    activeTexture(GL_TEXTURE0)

    for ((i, l) in activeLights.withIndex()) loadLight(i, l)

    glUniform1i(uLightCount, activeLights.size)

    matBuf.clear()
    ortho(0f, targetW.toFloat(), targetH.toFloat(), 0f, -1f, 1f).get(matBuf)
    matBuf.rewind()
    glUniformMatrix4fv(uMvp, false, matBuf)

    matBuf.clear()
    val mvp = Matrix4f(projection)
    mvp.mul(modelview)
    mvp.invert()
    mvp.get(matBuf)
    matBuf.rewind()
    glUniformMatrix4fv(uCamInv, false, matBuf)

    glViewport(0, 0, targetW, targetH)

    into.clear(MinecraftClient.IS_SYSTEM_MAC)
    into.beginWrite(false)
    depthMask(true)
    colorMask(true, true, true, true)

    val t = Tessellator.getInstance()
    val buf = t.buffer
    buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
    buf.vertex(0.0, 0.0, 0.0).next()
    buf.vertex(0.0, targetH.toDouble(), 0.0).next()
    buf.vertex(targetW.toDouble(), targetH.toDouble(), 0.0).next()
    buf.vertex(targetW.toDouble(), 0.0, 0.0).next()
    t.draw()

    into.endWrite()
    shader.disable()

    from.endRead()

    for (i in 2 until 3 + activeLights.size * 2) {
      activeTexture(GL_TEXTURE0 + i)
      disableTexture()
    }

    activeTexture(GL_TEXTURE0)
  }

  private fun loadLight(i: Int, l: LightContainer) {
    activeTexture(GL_TEXTURE3 + 2 * i)
    enableTexture()
    bindTexture(l.light.tex)

    activeTexture(GL_TEXTURE4 + 2 * i)
    enableTexture()
    l.depth.bind()

    glUniform1i(uLightTex[i], 3 + 2 * i)
    glUniform1i(uLightDepth[i], 4 + 2 * i)

    matBuf.clear()
    l.mvp.get(matBuf)
    matBuf.rewind()
    glUniformMatrix4fv(uLightCam[i], false, matBuf)

    glUniform3f(uLightPos[i], l.light.pos.x, l.light.pos.y, l.light.pos.z)
  }

  /**
   * Copies the contents of a [GlFramebuffer] into another [GlFramebuffer].
   */
  private fun blit(from: Framebuffer, into: Framebuffer) {
    glBindFramebuffer(GL_READ_FRAMEBUFFER, from.fbo)
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, into.fbo)
    glBlitFramebuffer(0, 0, from.textureWidth, from.textureHeight, 0, into.textureHeight, into.textureWidth, 0, GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT, GL_NEAREST)
    glBindFramebuffer(GL_READ_FRAMEBUFFER, 0)
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0)
  }

  /**
   * Copies depth buffer from a [GlFramebuffer] into a [WGlTexture2D].
   * Needed because MC stores depth information in a renderbuffer which can't be directly accessed.
   */
  private fun blitDepthToTex(from: Framebuffer, into: WGlTexture2D) {
    into.texImage(GL14.GL_DEPTH_COMPONENT24, from.textureWidth, from.textureHeight, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, 0)
    blitFb.bind(GL_DRAW_FRAMEBUFFER)
    into.bind()
    blitFb.attachTexture(GL_DEPTH_ATTACHMENT, into, target = GL_DRAW_FRAMEBUFFER)

    glBindFramebuffer(GL_READ_FRAMEBUFFER, from.fbo)

    glBlitFramebuffer(0, 0, from.textureWidth, from.textureHeight, 0, 0, from.textureWidth, from.textureHeight, GL_DEPTH_BUFFER_BIT, GL_NEAREST)

    glBindFramebuffer(GL_READ_FRAMEBUFFER, 0)
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0)

    from.beginWrite(true)
  }

  /**
   * Destroy the native resources this [PostProcess] object occupies. It will be unusable after this operation
   */
  fun destroy() {
    playerCamDepth.destroy()
    offscreenFb.delete()
    lightDepthFb.delete()
    blitFb.destroy()
    lights.values.forEach(LightContainer::destroy)
    lights.clear()
  }

}
