package therealfarfetchd.illuminate.client.test

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.mob.CreeperEntity
import org.joml.Vector3f
import therealfarfetchd.illuminate.client.api.Light
import therealfarfetchd.illuminate.client.api.Lights

class CreeperLight(val e: CreeperEntity) : Light {

  private var delta: Float = 0f

  override val tex = BlockLight.getTex().id

  override val pos: Vector3f
    get() {
      val pos = e.getCameraPosVec(delta)
      return Vector3f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
    }

  override val yaw: Float
    get() = -e.getYaw(delta) + 180f

  override val pitch: Float
    get() = e.getPitch(delta)

  override val fov: Float = 40f

  override fun prepare(delta: Float) {
    this.delta = delta

    if (e.isRemoved || e.world != MinecraftClient.getInstance().world) {
      Lights -= this
    }
  }

}
