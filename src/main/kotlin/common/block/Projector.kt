package therealfarfetchd.illuminate.common.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties.HORIZONTAL_FACING
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import therealfarfetchd.illuminate.client.api.Lights
import therealfarfetchd.illuminate.client.test.BlockLight

class ProjectorBlock : Block(Block.Settings.of(Material.METAL)) {

  override fun onBlockAdded(state: BlockState?, world: World?, pos: BlockPos, oldState: BlockState?, moved: Boolean) {
    MinecraftClient.getInstance().execute {
      Lights += BlockLight(pos)
    }
  }

  override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
    return this.defaultState.with(HORIZONTAL_FACING, ctx.playerFacing.opposite)
  }

  override fun rotate(state: BlockState, rotation: BlockRotation): BlockState =
    state.with(HORIZONTAL_FACING, rotation.rotate(state.get(HORIZONTAL_FACING)))

  override fun mirror(state: BlockState, mirror: BlockMirror): BlockState =
    state.rotate(mirror.getRotation(state.get(HORIZONTAL_FACING)))

  override fun appendProperties(b: StateManager.Builder<Block, BlockState>) {
    b.add(HORIZONTAL_FACING)
  }

}