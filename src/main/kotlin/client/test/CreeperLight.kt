package therealfarfetchd.illuminate.client.test

import net.dblsaiko.illuminate.client.IlluminateClient
import net.dblsaiko.illuminate.client.api.Light
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.mob.CreeperEntity
import org.joml.Vector3f

class CreeperLight(val e: CreeperEntity) : Light {

  private var delta: Float = 0f

  override fun tex() = BlockLight.getTex().id

  override fun pos(): Vector3f {
    val pos = e.getCameraPosVec(delta)
    return Vector3f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
  }

  override fun yaw(): Float = -e.getYaw(delta) + 180f

  override fun pitch(): Float = e.getPitch(delta)

  override fun fov(): Float = 40f

  override fun prepare(delta: Float) {
    this.delta = delta

    if (e.isRemoved || e.world != MinecraftClient.getInstance().world) {
      IlluminateClient.instance().removeLight(this)
    }
  }

}
