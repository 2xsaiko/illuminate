package therealfarfetchd.illuminate.common.init

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import therealfarfetchd.illuminate.ModID
import therealfarfetchd.illuminate.common.block.ProjectorBlock
import therealfarfetchd.illuminate.common.util.ext.makeStack

object ItemGroups {
  val all = FabricItemGroupBuilder.create(Identifier(ModID, "all"))
    .icon { Items.Projector.makeStack() }
    .build()
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
    return create(BlockItem(block, Item.Settings().group(ItemGroups.all)), name)
  }

  private fun <T : Item> create(item: T, name: String): T {
    return Registry.register(Registry.ITEM, Identifier(ModID, name), item)
  }

}