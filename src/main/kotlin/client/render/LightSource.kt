package therealfarfetchd.illuminate.client.render

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityCategory.MISC
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.world.World
import therealfarfetchd.illuminate.client.api.Light

class LightSource(world: World, light: Light) : Entity(Type, world) {

  init {
    setPosition(light.pos.x.toDouble(), light.pos.y.toDouble(), light.pos.z.toDouble())
    setRotation(light.yaw, light.pitch)

    this.prevX = this.x
    this.prevY = this.y
    this.prevZ = this.z
    this.prevPitch = this.pitch
    this.prevYaw = this.yaw
    this.prevRenderX = this.x
    this.prevRenderY = this.y
    this.prevRenderZ = this.z
  }

  override fun writeCustomDataToTag(var1: CompoundTag?) {}

  override fun readCustomDataFromTag(var1: CompoundTag?) {}

  override fun initDataTracker() {}

  override fun createSpawnPacket(): Packet<*>? = null

  companion object {
    val Type = EntityType<LightSource>({ _, _ -> error("can't construct") }, MISC, false, false, false, false, EntityDimensions.fixed(0f, 0f))
  }
}
