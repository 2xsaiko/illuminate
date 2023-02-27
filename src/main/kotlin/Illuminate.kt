package therealfarfetchd.illuminate

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import therealfarfetchd.illuminate.client.render.LightSource
import therealfarfetchd.illuminate.common.init.Blocks
import therealfarfetchd.illuminate.common.init.Items

const val ModID = "illuminate"

object Illuminate : ModInitializer {

  val Logger = LogManager.getLogger(ModID)

  val LightEntityType = Registry.register(
          Registries.ENTITY_TYPE,
          Identifier("illuminate", "light"),
          FabricEntityTypeBuilder.create(SpawnGroup.MISC, { _, _ -> error("can't construct") }).dimensions(EntityDimensions.fixed(0.6f, 0.6f)).build()
  )

  override fun onInitialize() {
    Blocks
    Items
  }

}
