package therealfarfetchd.illuminate

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import therealfarfetchd.illuminate.common.init.BlockEntityTypes
import therealfarfetchd.illuminate.common.init.Blocks
import therealfarfetchd.illuminate.common.init.Items
import therealfarfetchd.illuminate.common.util.ext.makeStack

const val ModID = "illuminate"

object Illuminate : ModInitializer {

  val Logger = LogManager.getLogger(ModID)

  override fun onInitialize() {
    BlockEntityTypes
    Blocks
    Items

    FabricItemGroupBuilder.create(Identifier(ModID, "all"))
      .icon { Items.Projector.makeStack() }
      .stacksForDisplay {
        it += Items.Projector.makeStack()
      }
      .build()
  }

}