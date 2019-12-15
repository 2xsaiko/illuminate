package therealfarfetchd.illuminate.client

import net.fabricmc.api.ClientModInitializer
import therealfarfetchd.illuminate.client.api.Lights
import therealfarfetchd.illuminate.client.init.Shaders
import therealfarfetchd.illuminate.client.render.LightsImpl

object IlluminateClient : ClientModInitializer {

  @Suppress("UNCHECKED_CAST")
  override fun onInitializeClient() {
    Shaders
    Lights.Instance = LightsImpl
  }

}