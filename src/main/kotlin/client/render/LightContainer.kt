package therealfarfetchd.illuminate.client.render

import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import therealfarfetchd.illuminate.client.api.Light
import therealfarfetchd.illuminate.client.glwrap.WGlTexture2D
import therealfarfetchd.illuminate.common.util.ext.perspective
import therealfarfetchd.illuminate.common.util.ext.times
import therealfarfetchd.illuminate.common.util.ext.unaryMinus

data class LightContainer(val light: Light) {

  val depth = WGlTexture2D.create()

  init {
    depth.texParameter(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
    depth.texParameter(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
    depth.texParameter(GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
    depth.texParameter(GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
  }

  private var cachedPos = light.pos
    set(value) {
      mvpDirty = mvpDirty || field != value
      field = value
    }

  private var cachedYaw = light.yaw
    set(value) {
      mvDirty = mvDirty || field != value
      field = value
    }

  private var cachedPitch = light.pitch
    set(value) {
      mvDirty = mvDirty || field != value
      field = value
    }

  private var cachedRoll = light.roll
    set(value) {
      mvDirty = mvDirty || field != value
      field = value
    }

  private var cachedFov = light.fov
    set(value) {
      pDirty = pDirty || field != value
      field = value
    }

  private var cachedAspect = light.aspect
    set(value) {
      pDirty = pDirty || field != value
      field = value
    }

  private var cachedNear = light.near
    set(value) {
      pDirty = pDirty || field != value
      field = value
    }

  private var cachedFar = light.far
    set(value) {
      pDirty = pDirty || field != value
      field = value
    }

  fun update() {
    cachedPos = light.pos
    cachedYaw = light.yaw
    cachedPitch = light.pitch
    cachedRoll = light.roll
    cachedFov = light.fov
    cachedAspect = light.aspect
    cachedNear = light.near
    cachedFar = light.far
  }

  private var mvpDirty = true

  private var mvDirty = true
    set(value) {
      field = value
      if (value) mvpDirty = true
    }

  private var pDirty = true
    set(value) {
      field = value
      if (value) mvpDirty = true
    }

  var mv: Matrix4f = Matrix4f()
    get() {
      update()
      if (mvDirty) {
        field = Matrix4f()
          .rotate(0f, 0f, 1f, cachedRoll)
          .rotate(1f, 0f, 0f, cachedPitch)
          .rotate(0f, 1f, 0f, cachedYaw)
        mvDirty = false
      }
      return field
    }
    private set

  var p: Matrix4f = Matrix4f()
    get() {
      update()
      if (pDirty) {
        field = perspective(cachedFov, cachedAspect, cachedNear, cachedFar)
        pDirty = false
      }
      return field
    }
    private set

  var mvp: Matrix4f = Matrix4f()
    get() {
      update()
      if (mvpDirty) {
        field = p.mul(mv.translate(-cachedPos))
        mvpDirty = false
      }
      return field
    }
    private set

  fun destroy() {
    depth.destroy()
  }

}
