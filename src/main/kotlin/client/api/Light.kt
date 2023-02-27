package therealfarfetchd.illuminate.client.api

import org.joml.Vector3f

/**
 * An oriented light which projects a texture.
 */
interface Light {

  /**
   * A GL texture id representing the texture that should be projected.
   */
  val tex: Int

  /**
   * The origin point of the light
   */
  val pos: Vector3f

  /**
   * The yaw rotation of the light
   */
  val yaw: Float

  /**
   * The pitch rotation of the light
   */
  @JvmDefault
  val pitch: Float
    get() = 0f

  /**
   * The roll rotation of the light
   */
  @JvmDefault
  val roll: Float
    get() = 0f

  /**
   * The vertical field of view (FoV) of the light
   */
  @JvmDefault
  val fov: Float
    get() = 90f

  /**
   * The aspect ratio of the light (X:Y)
   */
  @JvmDefault
  val aspect: Float
    get() = 1f

  /**
   * The distance of the near clip plane of the projection from the origin
   */
  @JvmDefault
  val near: Float
    get() = 0.8660254f

  /**
   * The distance of the far clip plane of the projection from the origin
   */
  @JvmDefault
  val far: Float
    get() = 50f

  /**
   * Prepare the light for rendering. This gets called before the world gets drawn from the light's perspective
   */
  @JvmDefault
  fun prepare(delta: Float) {
  }

}
