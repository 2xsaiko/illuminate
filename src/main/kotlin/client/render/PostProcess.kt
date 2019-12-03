package therealfarfetchd.illuminate.client.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.systems.RenderSystem.activeTexture
import com.mojang.blaze3d.systems.RenderSystem.bindTexture
import com.mojang.blaze3d.systems.RenderSystem.clear
import com.mojang.blaze3d.systems.RenderSystem.colorMask
import com.mojang.blaze3d.systems.RenderSystem.depthMask
import com.mojang.blaze3d.systems.RenderSystem.disableCull
import com.mojang.blaze3d.systems.RenderSystem.disableTexture
import com.mojang.blaze3d.systems.RenderSystem.enableTexture
import com.mojang.blaze3d.systems.RenderSystem.popMatrix
import com.mojang.blaze3d.systems.RenderSystem.pushMatrix
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.Identifier
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_NEAREST
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.opengl.GL11.glDrawBuffer
import org.lwjgl.opengl.GL11.glGetFloatv
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
import therealfarfetchd.illuminate.ModID
import therealfarfetchd.illuminate.client.api.Light
import therealfarfetchd.illuminate.client.glwrap.WGlFramebuffer
import therealfarfetchd.illuminate.client.glwrap.WGlShader
import therealfarfetchd.illuminate.client.glwrap.WGlTexture2D
import therealfarfetchd.illuminate.client.setFramebuffer
import therealfarfetchd.illuminate.common.util.ext.minus
import therealfarfetchd.illuminate.common.util.ext.ortho
import therealfarfetchd.illuminate.common.util.ext.times
import therealfarfetchd.qcommon.croco.Mat4
import therealfarfetchd.qcommon.croco.Vec3
import java.io.IOException

private val matBuf = BufferUtils.createFloatBuffer(16)

@Suppress("PrivatePropertyName")
class PostProcess(private val mc: MinecraftClient) {

  private val target = mc.framebuffer

  private val width = target.viewportWidth
  private val height = target.viewportHeight

  private val shader: WGlShader

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

  private val offscreenFb = Framebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC)
  private val lightDepthFb = LightFramebuffer(1024, 1024, false)
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

    shader = try {
      val vshs = mc.resourceManager.getResource(Identifier(ModID, "shaders/lighting.vert")).use { it.inputStream.bufferedReader().readText() }
      val fshs = mc.resourceManager.getResource(Identifier(ModID, "shaders/lighting.frag")).use { it.inputStream.bufferedReader().readText() }

      mkShader(vshs, fshs)
    } catch (e: IOException) {
      WGlShader.None
    }

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

    blitFb.bind()
    glDrawBuffer(GL11.GL_NONE)
    mc.framebuffer.beginWrite(false)
  }

  /**
   * Determine which lights should be rendered from the player's position
   */
  fun setupLights(delta: Float) {
    activeLights =
      if (lights.size < 10) lights.values.toSet()
      else {
        val camPos = Vec3.from(mc.gameRenderer.camera.pos)
        lights.values.asSequence().sortedBy { (it.light.pos - camPos).lengthSq }.take(10).toSet()
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
  fun renderLightDepths(delta: Float, nanoTime: Long) {
    if (activeLights.isEmpty()) return

    val oldHudHidden = mc.options.hudHidden
    val oldCam = mc.cameraEntity
    mc.options.hudHidden = true
    val window = mc.framebuffer
    for ((i, lc) in activeLights.withIndex()) {
      lightDepthFb.beginWrite(true)
      glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
      mc.setFramebuffer(lightDepthFb)

      glMatrixMode(GL11.GL_PROJECTION)
      pushMatrix()
      glMatrixMode(GL11.GL_MODELVIEW)
      pushMatrix()

      lc.light.prepare(delta)

      setupCamera(lc)
      val lightSource = LightSource(mc.world!!, lc.light)
      mc.cameraEntity = lightSource
      disableCull()
      renderWorld(mc.gameRenderer, delta, nanoTime, lc.light, i)
      blitDepthToTex(lightDepthFb, lc.depth)

      glMatrixMode(GL11.GL_PROJECTION)
      popMatrix()
      glMatrixMode(GL11.GL_MODELVIEW)
      popMatrix()

    }
    mc.setFramebuffer(window)
    window.beginWrite(true)

    mc.cameraEntity = oldCam
    mc.options.hudHidden = oldHudHidden
  }

  /**
   * Applies the shader onto the target framebuffer
   */
  fun paintSurfaces(delta: Float) {
    if (activeLights.isEmpty()) return
    if (!shader.isValid) return // something is fucked

    val ce = mc.cameraEntity!!
    val camera = mc.gameRenderer.camera
    val cameraPosVec = camera.pos
    RenderSystem.translated(-cameraPosVec.x, -cameraPosVec.y, -cameraPosVec.z)

    blitDepthToTex(target, playerCamDepth)

    clear(GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC)

    paintSurfaces(target, offscreenFb)
    blit(offscreenFb, target)

    target.beginWrite(true)
  }

  /**
   * Apply the shader onto the source framebuffer and draw into the target framebuffer
   */
  private fun paintSurfaces(from: Framebuffer, into: Framebuffer) {
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
    ortho(0f, targetW.toFloat(), targetH.toFloat(), 0f, -1f, 1f).intoBuffer(matBuf)
    matBuf.rewind()
    glUniformMatrix4fv(uMvp, false, matBuf)

    matBuf.clear()
    glGetFloatv(GL11.GL_MODELVIEW_MATRIX, matBuf)
    val mv = Mat4.fromBuffer(matBuf)
    matBuf.clear()
    glGetFloatv(GL11.GL_PROJECTION_MATRIX, matBuf)
    val p = Mat4.fromBuffer(matBuf)
    matBuf.clear()
    (p * mv).invert().intoBuffer(matBuf)
    matBuf.rewind()
    glUniformMatrix4fv(uCamInv, false, matBuf)

    glViewport(0, 0, targetW, targetH)

    into.clear(MinecraftClient.IS_SYSTEM_MAC)
    into.beginWrite(false)
    depthMask(true)
    colorMask(true, true, true, true)

    val t = Tessellator.getInstance()
    val buf = t.buffer
    buf.begin(GL_QUADS, VertexFormats.POSITION)
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
    l.mvp.intoBuffer(matBuf)
    matBuf.rewind()
    glUniformMatrix4fv(uLightCam[i], false, matBuf)

    glUniform3f(uLightPos[i], l.light.pos.x, l.light.pos.y, l.light.pos.z)
  }

  /**
   * Sets up GL's modelview and projection matrices according to the information from the passed [Light]
   */
  private fun setupCamera(light: LightContainer) {
    //    val matBuf = matBuf
    //
    //    matBuf.clear()
    //    light.p.intoBuffer(matBuf)
    //    matBuf.rewind()
    //    matrixMode(GL11.GL_PROJECTION)
    //    loadIdentity()
    //    multMatrix(matBuf)
    //
    //    matBuf.clear()
    //    light.mv.intoBuffer(matBuf)
    //    matBuf.rewind()
    //    matrixMode(GL11.GL_MODELVIEW)
    //    loadIdentity()
    //    multMatrix(matBuf)
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
    shader.destroy()
    playerCamDepth.destroy()
    offscreenFb.delete()
    lightDepthFb.delete()
    blitFb.destroy()
    lights.values.forEach(LightContainer::destroy)
    lights.clear()
  }

}