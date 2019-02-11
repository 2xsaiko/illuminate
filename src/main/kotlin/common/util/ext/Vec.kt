package therealfarfetchd.illuminate.common.util.ext

import therealfarfetchd.qcommon.croco.Mat4
import therealfarfetchd.qcommon.croco.Vec3
import java.lang.Math.toRadians
import kotlin.math.tan

operator fun Vec3.minus(other: Vec3) = sub(other)
operator fun Vec3.plus(other: Vec3) = add(other)
operator fun Vec3.unaryMinus() = negate()
operator fun Vec3.unaryPlus() = this

operator fun Mat4.times(other: Mat4) = mul(other)

fun perspective(fovY: Float, aspect: Float, zNear: Float, zFar: Float): Mat4 {
  val halfFovyRadians = toRadians((fovY / 2f).toDouble()).toFloat()
  val range = tan(halfFovyRadians) * zNear
  val left = -range * aspect
  val right = range * aspect
  val bottom = -range

  return Mat4(
    2f * zNear / (right - left), 0f, 0f, 0f,
    0f, 2f * zNear / (range - bottom), 0f, 0f,
    0f, 0f, (-(zFar + zNear) / (zFar - zNear)), -(2f * zFar * zNear) / (zFar - zNear),
    0f, 0f, -1f, 0f
  )
}

fun ortho(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float): Mat4 {
  val m00 = 2f / (right - left)
  val m11 = 2f / (top - bottom)
  val m22 = -2f / (zFar - zNear)
  val m03 = -(right + left) / (right - left)
  val m13 = -(top + bottom) / (top - bottom)
  val m23 = -(zFar + zNear) / (zFar - zNear)

  return Mat4(
    m00, 0f, 0f, m03,
    0f, m11, 0f, m13,
    0f, 0f, m22, m23,
    0f, 0f, 0f, 1f
  )
}