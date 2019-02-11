package therealfarfetchd.illuminate.common.init

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.block.BlockItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import therealfarfetchd.illuminate.ModID
import therealfarfetchd.illuminate.common.block.ProjectorBlock

object BlockEntityTypes {

  private fun <T : BlockEntity> create(builder: () -> T, name: String): BlockEntityType<T> {
    return Registry.register(Registry.BLOCK_ENTITY, Identifier(ModID, name), BlockEntityType.Builder.create(builder).build(null))
  }

}

object Blocks {

  val Projector = create(ProjectorBlock(), "projector")

  private fun <T : Block> create(block: T, name: String): T {
    return Registry.register(Registry.BLOCK, Identifier(ModID, name), block)
  }

}

object Items {

  val Projector = create(Blocks.Projector, "projector")

  private fun <T : Block> create(block: T, name: String): BlockItem {
    return create(BlockItem(block, Item.Settings().itemGroup(ItemGroup.REDSTONE)), name)
  }

  private fun <T : Item> create(item: T, name: String): T {
    return Registry.register(Registry.ITEM, Identifier(ModID, name), item)
  }

}