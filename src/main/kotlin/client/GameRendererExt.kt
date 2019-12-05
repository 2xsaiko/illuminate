package therealfarfetchd.illuminate.client

import net.minecraft.client.render.GameRenderer
import therealfarfetchd.illuminate.client.render.LightContainer
import therealfarfetchd.illuminate.client.render.PostProcess

interface GameRendererExt {

  val postProcess: PostProcess

  var activeRenderLight: LightContainer?

}

val GameRenderer.postProcess
  get() = (this as GameRendererExt).postProcess

var GameRenderer.activeRenderLight
  get() = (this as GameRendererExt).activeRenderLight
  set(value) {
    (this as GameRendererExt).activeRenderLight = value
  }