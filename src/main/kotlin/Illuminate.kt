package therealfarfetchd.illuminate

import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import therealfarfetchd.illuminate.common.init.Blocks
import therealfarfetchd.illuminate.common.init.Items

const val ModID = "illuminate"

object Illuminate : ModInitializer {

  val Logger = LogManager.getLogger(ModID)

  override fun onInitialize() {
    Blocks
    Items
  }

}