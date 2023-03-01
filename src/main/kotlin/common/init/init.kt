package therealfarfetchd.illuminate.common.init

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import therealfarfetchd.illuminate.ModID
import therealfarfetchd.illuminate.common.block.ProjectorBlock

//object ItemGroups {
//  val all = FabricItemGroupBuilder.create(Identifier(ModID, "all"))
//    .icon { Items.Projector.makeStack() }
//    .build()
//}

object Blocks {

  val Projector = create(ProjectorBlock(), "projector")

  private fun <T : Block> create(block: T, name: String): T {
    return Registry.register(Registries.BLOCK, Identifier(ModID, name), block)
  }

}

object Items {

  val Projector = create(Blocks.Projector, "projector")

  private fun <T : Block> create(block: T, name: String): BlockItem {
    return create(BlockItem(block, Item.Settings()), name)
  }

  private fun <T : Item> create(item: T, name: String): T {
    return Registry.register(Registries.ITEM, Identifier(ModID, name), item)
  }

}
