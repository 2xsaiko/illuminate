package therealfarfetchd.illuminate.common.util.ext

import org.joml.Matrix4f
import org.joml.Vector3f
import java.lang.Math.toRadians
import kotlin.math.tan

operator fun Vector3f.minus(other: Vector3f) = sub(other)
operator fun Vector3f.plus(other: Vector3f) = add(other)
operator fun Vector3f.unaryMinus() = negate()
operator fun Vector3f.unaryPlus() = this

operator fun Matrix4f.times(other: Matrix4f) = mul(other)

fun perspective(fovY: Float, aspect: Float, zNear: Float, zFar: Float): Matrix4f {
  val halfFovyRadians = toRadians((fovY / 2f).toDouble()).toFloat()
  val range = tan(halfFovyRadians) * zNear
  val left = -range * aspect
  val right = range * aspect
  val bottom = -range

  return Matrix4f(
    2f * zNear / (right - left), 0f, 0f, 0f,
    0f, 2f * zNear / (range - bottom), 0f, 0f,
    0f, 0f, (-(zFar + zNear) / (zFar - zNear)), -(2f * zFar * zNear) / (zFar - zNear),
    0f, 0f, -1f, 0f
  )
}

fun ortho(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float): Matrix4f {
  val m00 = 2f / (right - left)
  val m11 = 2f / (top - bottom)
  val m22 = -2f / (zFar - zNear)
  val m03 = -(right + left) / (right - left)
  val m13 = -(top + bottom) / (top - bottom)
  val m23 = -(zFar + zNear) / (zFar - zNear)

  return Matrix4f(
    m00, 0f, 0f, m03,
    0f, m11, 0f, m13,
    0f, 0f, m22, m23,
    0f, 0f, 0f, 1f
  )
}
