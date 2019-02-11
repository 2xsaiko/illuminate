package therealfarfetchd.illuminate.client

import net.minecraft.client.render.GameRenderer
import therealfarfetchd.illuminate.client.render.PostProcess

interface GameRendererExt {

  val postProcess: PostProcess

}

val GameRenderer.postProcess
  get() = (this as GameRendererExt).postProcess