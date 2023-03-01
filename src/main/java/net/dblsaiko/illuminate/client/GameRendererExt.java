package net.dblsaiko.illuminate.client;

import net.dblsaiko.illuminate.client.render.PostProcess;
import net.minecraft.client.render.GameRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GameRendererExt {
    @NotNull
    PostProcess postProcess();

    @Nullable
    LightContainer activeRenderLight();

    void setActiveRenderLight(LightContainer lc);

    static GameRendererExt from(GameRenderer self) {
        return (GameRendererExt) self;
    }
}
