package therealfarfetchd.illuminate.client.render

import com.google.common.collect.ImmutableSet
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.world.World
import therealfarfetchd.illuminate.Illuminate
import therealfarfetchd.illuminate.client.api.Light

class LightSource(world: World, light: Light) : Entity(Illuminate.LightEntityType, world) {

  init {
    setPosition(light.pos.x.toDouble(), light.pos.y.toDouble(), light.pos.z.toDouble())
    setRotation(light.yaw, light.pitch)

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
