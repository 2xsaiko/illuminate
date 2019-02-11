package therealfarfetchd.illuminate.client.test

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.mob.CreeperEntity
import therealfarfetchd.illuminate.client.api.Light
import therealfarfetchd.illuminate.client.api.Lights
import therealfarfetchd.qcommon.croco.Vec3

class CreeperLight(val e: CreeperEntity) : Light {

  private var delta: Float = 0f

  override val tex = BlockLight.getTex().id

  override val pos: Vec3
    get() = Vec3.from(e.getCameraPosVec(delta))

  override val yaw: Float
    get() = -e.getYaw(delta) + 180f

  override val pitch: Float
    get() = e.getPitch(delta)

  override val fov: Float = 40f

  override fun prepare(delta: Float) {
    this.delta = delta

    if (!e.isValid || e.world != MinecraftClient.getInstance().world) {
      Lights -= this
    }
  }

}