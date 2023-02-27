package therealfarfetchd.illuminate.client.test

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.joml.Vector3f
import therealfarfetchd.illuminate.ModID
import therealfarfetchd.illuminate.client.api.Light
import therealfarfetchd.illuminate.client.api.Lights
import therealfarfetchd.illuminate.client.glwrap.WGlTexture2D
import therealfarfetchd.illuminate.common.init.Blocks
import kotlin.math.sqrt

class BlockLight(val bp: BlockPos) : Light {

  override val tex = getTex().id

  override val pos: Vector3f = Vector3f(bp.x + 0.5f, bp.y + 0.5f, bp.z + 0.5f)

  override var yaw: Float = 80f

  override val fov: Float = 35f

  override val aspect: Float = 768/576f

  override val near: Float = sqrt(2f) / 2

  private var wait = true

  override fun prepare(delta: Float) {
    val state = MinecraftClient.getInstance().world!!.getBlockState(bp)

    if (state.block != Blocks.Projector) {
      if (!wait) Lights -= this
      return
    } else {
      wait = false
    }

    yaw = -state.get(Properties.HORIZONTAL_FACING).asRotation()
  }

  companion object {
    fun getTex(): WGlTexture2D {
      val tm = MinecraftClient.getInstance().textureManager
      val identifier = Identifier(ModID, "textures/test.png")
      tm.bindTexture(identifier)
      RenderSystem.bindTexture(0)

      return WGlTexture2D(tm.getTexture(identifier)!!.glId)
    }
  }

}
