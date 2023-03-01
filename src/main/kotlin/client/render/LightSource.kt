package therealfarfetchd.illuminate.client.render

import net.dblsaiko.illuminate.client.api.Light
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.world.World
import therealfarfetchd.illuminate.Illuminate

class LightSource(world: World, light: Light) : Entity(Illuminate.LightEntityType, world) {

  init {
    setPosition(light.pos().x().toDouble(), light.pos().y().toDouble(), light.pos().z().toDouble())
    setRotation(light.yaw(), light.pitch())

    this.prevX = this.x
    this.prevY = this.y
    this.prevZ = this.z
    this.prevPitch = this.pitch
    this.prevYaw = this.yaw
    this.lastRenderX = this.x
    this.lastRenderY = this.y
    this.lastRenderZ = this.z
  }

  override fun initDataTracker() {}

  override fun readCustomDataFromNbt(nbt: NbtCompound?) {}

  override fun writeCustomDataToNbt(nbt: NbtCompound?) {}

  override fun createSpawnPacket(): Packet<ClientPlayPacketListener>? = null
}
