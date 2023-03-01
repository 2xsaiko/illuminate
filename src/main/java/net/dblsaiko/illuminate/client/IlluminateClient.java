package net.dblsaiko.illuminate.client;

import net.dblsaiko.illuminate.client.api.Light;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IlluminateClient {
    private static IlluminateClient instance = null;

    private final MinecraftClient mc;

    public final Shaders shaders;

    public IlluminateClient(MinecraftClient mc) {
        this.mc = mc;
        this.shaders = new Shaders(mc);
    }

    public static void initializeClient() {
        if (instance != null) {
            throw new IllegalStateException("IlluminateClient::initializeClient called twice!");
        }

        var c = new IlluminateClient(Objects.requireNonNull(MinecraftClient.getInstance()));
        c.initialize();
        instance = c;
    }

    @NotNull
    public static IlluminateClient instance() {
        return Objects.requireNonNull(instance);
    }

    private void initialize() {
        this.shaders.initialize();
    }

    public boolean addLight(Light light) {
        return GameRendererExt.from(this.mc.gameRenderer).postProcess().addLight(light);
    }

    public boolean removeLight(Light light) {
        return GameRendererExt.from(this.mc.gameRenderer).postProcess().removeLight(light);
    }

    public void onJoinWorld() {
        GameRendererExt.from(this.mc.gameRenderer).postProcess().onJoinWorld();
    }
}
