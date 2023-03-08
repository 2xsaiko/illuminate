package net.dblsaiko.illuminate.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.dblsaiko.illuminate.Illuminate;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class Shaders {
    private static final Logger LOGGER = LogManager.getLogger(Shaders.class);

    private final IlluminateClient ic;
    private final MinecraftClient mc;

    private LightingShader lighting;
    private ComposeShader compose;

    public Shaders(IlluminateClient ic, MinecraftClient mc) {
        this.ic = ic;
        this.mc = mc;
    }

    public void initialize() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Illuminate.id("shaders");
            }

            @Override
            public CompletableFuture<Void> reload(
                    Synchronizer synchronizer,
                    ResourceManager manager,
                    Profiler prepareProfiler,
                    Profiler applyProfiler,
                    Executor prepareExecutor,
                    Executor applyExecutor
            ) {
                return CompletableFuture
                        .runAsync(() -> Shaders.this.reloadShaders(manager), applyExecutor)
                        .thenCompose(n -> synchronizer.whenPrepared(null));
            }
        });
    }

    public LightingShader lighting() {
        return this.lighting;
    }

    public ComposeShader compose() {
        return this.compose;
    }

    private void reloadShaders(ResourceManager rm) {
        if (this.lighting != null) {
            GlStateManager.glDeleteProgram(this.lighting.id());
        }

        if (this.compose != null) {
            GlStateManager.glDeleteProgram(this.compose.id());
        }

        int lighting = this.loadShader(rm, "lighting");

        if (lighting != 0) {
            this.lighting = new LightingShader(lighting);
            this.lighting.setup();
        }

        int compose = this.loadShader(rm, "compose");

        if (compose != 0) {
            this.compose = new ComposeShader(compose);
            this.compose.setup();
        }

        GameRendererExt.from(this.mc.gameRenderer).postProcess().onShaderReload();
    }

    private int loadShader(ResourceManager rm, String id) {
        String vshs = this.readShader(rm, "shaders/%s.vert".formatted(id));
        String fshs = this.readShader(rm, "shaders/%s.frag".formatted(id));

        int vsh = GlStateManager.glCreateShader(GL31.GL_VERTEX_SHADER);
        int fsh = GlStateManager.glCreateShader(GL31.GL_FRAGMENT_SHADER);
        int prog = GlStateManager.glCreateProgram();

        do {
            GL31.glShaderSource(vsh, vshs);
            GL31.glShaderSource(fsh, fshs);

            if (!this.compileShader(vsh, id, "vertex")) {
                break;
            }

            if (!this.compileShader(fsh, id, "fragment")) {
                break;
            }

            GlStateManager.glAttachShader(prog, vsh);
            GlStateManager.glAttachShader(prog, fsh);
            GlStateManager.glLinkProgram(prog);

            if (GlStateManager.glGetProgrami(prog, GL31.GL_LINK_STATUS) == GL11.GL_FALSE) {
                var log = GlStateManager.glGetProgramInfoLog(prog, 32768);
                LOGGER.error("Failed to link program '{}'", id);

                for (String line : (Iterable<String>) () -> log.lines().iterator()) {
                    LOGGER.error("{}", line);
                }

                break;
            }

            GlStateManager.glDeleteShader(fsh);
            GlStateManager.glDeleteShader(vsh);

            LOGGER.info("Loaded shader '{}' with id '{}'", id, prog);
            return prog;
        } while (false);

        // Error handling case
        GlStateManager.glDeleteProgram(prog);
        GlStateManager.glDeleteShader(fsh);
        GlStateManager.glDeleteProgram(vsh);

        LOGGER.error("Failed to load shader '{}'\n", id);
        return 0;
    }

    private boolean compileShader(int shader, String id, String type) {
        GlStateManager.glCompileShader(shader);

        if (GlStateManager.glGetShaderi(shader, GL31.GL_COMPILE_STATUS) == GL31.GL_FALSE) {
            var log = GlStateManager.glGetShaderInfoLog(shader, 32768);
            LOGGER.error("Failed to compile {} shader '{}'\n", type, id);

            for (String line : (Iterable<String>) () -> log.lines().iterator()) {
                LOGGER.error("{}", line);
            }

            return false;
        }

        return true;
    }

    private String readShader(ResourceManager rm, String id) {
        try (BufferedReader r = rm.getResourceOrThrow(Illuminate.id(id)).getReader()) {
            StringBuilder sb = new StringBuilder();
            String buf;

            while ((buf = r.readLine()) != null) {
                sb.append(buf).append('\n');

                if (buf.startsWith("#version ")) {
                    sb.append("#define MAX_LIGHTS ").append(this.ic.getMaxLights()).append('\n');
                }
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
