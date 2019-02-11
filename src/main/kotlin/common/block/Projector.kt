package therealfarfetchd.illuminate.common.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateFactory.Builder
import net.minecraft.state.property.Properties.FACING_HORIZONTAL
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import therealfarfetchd.illuminate.client.api.Lights
import therealfarfetchd.illuminate.client.test.BlockLight

class ProjectorBlock : Block(Block.Settings.of(Material.METAL)) {

  override fun onBlockAdded(blockState_1: BlockState?, world_1: World?, blockPos_1: BlockPos, blockState_2: BlockState?) {
    MinecraftClient.getInstance().execute {
      Lights += BlockLight(blockPos_1)
    }
  }

  override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
    return this.defaultState.with(FACING_HORIZONTAL, ctx.playerHorizontalFacing.opposite)
  }

  override fun rotate(state: BlockState, rotation: Rotation): BlockState =
    state.with(FACING_HORIZONTAL, rotation.rotate(state.get<Direction>(FACING_HORIZONTAL) as Direction))

  override fun mirror(state: BlockState, mirror: Mirror): BlockState =
    state.rotate(mirror.getRotation(state.get<Direction>(FACING_HORIZONTAL) as Direction))

  override fun appendProperties(b: Builder<Block, BlockState>) {
    b.with(FACING_HORIZONTAL)
  }

}