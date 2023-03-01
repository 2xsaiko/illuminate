package therealfarfetchd.illuminate.client

import net.dblsaiko.illuminate.client.GameRendererExt
import net.minecraft.client.render.GameRenderer

val GameRenderer.postProcess
    get() = GameRendererExt.from(this).postProcess()

var GameRenderer.activeRenderLight
    get() = GameRendererExt.from(this).activeRenderLight()
    set(value) {
        GameRendererExt.from(this).setActiveRenderLight(value)
    }