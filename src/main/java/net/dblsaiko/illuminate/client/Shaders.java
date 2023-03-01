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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class Shaders {
    private final MinecraftClient mc;

    private int lighting = 0;

    public Shaders(MinecraftClient mc) {
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

    public int lighting() {
        return this.lighting;
    }

    private void reloadShaders(ResourceManager rm) {
        if (this.lighting != 0) {
            GlStateManager.glDeleteProgram(this.lighting);
        }

        this.lighting = this.loadShader(rm, "lighting");

        GameRendererExt.from(this.mc.gameRenderer).postProcess().onShaderReload();
    }

    private int loadShader(ResourceManager rm, String id) {
        String vshs = this.readResource(rm, "shaders/%s.vert".formatted(id));
        String fshs = this.readResource(rm, "shaders/%s.frag".formatted(id));

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
                System.out.printf("Failed to link program '%s'\n", id);

                for (String line : (Iterable<String>) () -> log.lines().iterator()) {
                    System.out.println(line);
                }

                break;
            }

            GlStateManager.glDeleteShader(fsh);
            GlStateManager.glDeleteShader(vsh);

            return prog;
        } while (false);

        // Error handling case
        GlStateManager.glDeleteProgram(prog);
        GlStateManager.glDeleteShader(fsh);
        GlStateManager.glDeleteProgram(vsh);
        return 0;
    }

    private boolean compileShader(int shader, String id, String type) {
        GlStateManager.glCompileShader(shader);

        if (GlStateManager.glGetShaderi(shader, GL31.GL_COMPILE_STATUS) == GL31.GL_FALSE) {
            var log = GlStateManager.glGetShaderInfoLog(shader, 32768);
            System.out.printf("Failed to compile %s shader '%s'\n", type, id);

            for (String line : (Iterable<String>) () -> log.lines().iterator()) {
                System.out.println(line);
            }

            return false;
        }

        return true;
    }

    private String readResource(ResourceManager rm, String id) {
        try (BufferedReader r = rm.getResourceOrThrow(Illuminate.id(id)).getReader()) {
            StringBuilder sb = new StringBuilder();
            String buf;

            while ((buf = r.readLine()) != null) {
                sb.append(buf).append('\n');
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
