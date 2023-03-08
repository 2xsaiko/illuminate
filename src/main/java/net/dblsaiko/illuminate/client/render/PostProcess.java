package net.dblsaiko.illuminate.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dblsaiko.illuminate.client.*;
import net.dblsaiko.illuminate.client.api.Light;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL31;

import java.nio.FloatBuffer;
import java.util.*;

public class PostProcess {
    private static final Logger LOGGER = LogManager.getLogger(PostProcess.class);

    private static final FloatBuffer MAT_BUF = BufferUtils.createFloatBuffer(16);

    private final MinecraftClient mc;
    private final Framebuffer target;

    private final SimpleFramebuffer offscreenFb;
    private final SimpleFramebuffer offscreenFb2;
    private final LightFramebuffer lightDepthFb = new LightFramebuffer(1024, 1024, MinecraftClient.IS_SYSTEM_MAC);
    private int blitFb;

    private int playerCamDepthTex;

    private final Map<Light, LightContainer> lights = new HashMap<>();
    private final Set<LightContainer> activeLights = new HashSet<>();
    private final Set<LightContainer> activeLightsView = Collections.unmodifiableSet(this.activeLights);

    private int maxTextures = -1;

    public PostProcess(MinecraftClient mc) {
        this.mc = mc;
        this.target = mc.getFramebuffer();
        this.offscreenFb = new SimpleFramebuffer(this.target.viewportWidth, this.target.viewportHeight, true, MinecraftClient.IS_SYSTEM_MAC);
        this.offscreenFb2 = new SimpleFramebuffer(this.target.viewportWidth, this.target.viewportHeight, true, MinecraftClient.IS_SYSTEM_MAC);
        this.offscreenFb.setClearColor(0, 0, 0, 0);
        this.offscreenFb2.setClearColor(0, 0, 0, 0);
    }

    public int getMaxTextures() {
        if (this.maxTextures == -1) {
            // Not sure whether this is the right property to query; there's
            // none for fragment shaders which is where the textures actually
            // get sampled, but it seems to return the right value. The standard
            // says this should be at least 16, so it isn't necessary to handle
            // the case where this would calculate a maxTextures value of 0 or
            // lower.
            int maxTextures = GL31.glGetInteger(GL31.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);
            this.maxTextures = maxTextures - 2;

            LOGGER.info("Graphics driver supports a maximum of {} textures, using a maximum of {} for lights", maxTextures, this.maxTextures);
        }

        return this.maxTextures;
    }

    public int playerCamDepthTex() {
        if (this.playerCamDepthTex == 0) {
            this.playerCamDepthTex = GlStateManager._genTexture();
            RenderSystem.bindTexture(this.playerCamDepthTex);
            RenderSystem.texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_NEAREST);
            RenderSystem.texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_NEAREST);
            RenderSystem.texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_S, GL31.GL_CLAMP_TO_EDGE);
            RenderSystem.texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_T, GL31.GL_CLAMP_TO_EDGE);
        }

        return this.playerCamDepthTex;
    }

    private int blitFb() {
        if (this.blitFb == 0) {
            this.blitFb = GlStateManager.glGenFramebuffers();
        }

        return this.blitFb;
    }

    public boolean addLight(Light light) {
        if (this.lights.containsKey(light)) {
            return false;
        }

        this.lights.put(light, new LightContainer(light));
        return true;
    }

    public boolean removeLight(Light light) {
        LightContainer lc = this.lights.remove(light);

        if (lc != null) {
            lc.destroy();
            return true;
        }

        return false;
    }

    public Set<LightContainer> activeLights() {
        return this.activeLightsView;
    }

    public void onShaderReload() {
        LightingShader shader = IlluminateClient.instance().shaders.lighting();

        if (shader != null) {
            GlStateManager._glUseProgram(shader.id());

            for (int i = 0; i < shader.texTable.length(); i += 1) {
                // These always have the same values, so set it here
                RenderSystem.glUniform1i(shader.texTable.index(i), 3 + i);
            }
        }
    }

    /**
     * Determine which lights should be rendered from the player's position
     */
    public void setupLights(float delta) {
        this.activeLights.clear();

        this.activeLights.addAll(this.lights.values());
    }

    /**
     * Resize the framebuffers and textures to the specified size.
     */
    public void resize(int width, int height) {
        this.offscreenFb.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
        this.offscreenFb2.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
    }

    public void renderLightDepths(float delta, long nanoTime, MatrixStack mv) {
        if (this.activeLights.isEmpty()) {
            return;
        }

        boolean oldHudHidden = this.mc.options.hudHidden;
        Entity oldCam = this.mc.cameraEntity;
        Perspective oldPerspective = this.mc.options.getPerspective();

        this.mc.options.hudHidden = true;
        this.mc.options.setPerspective(Perspective.FIRST_PERSON);

        for (LightContainer lc : this.activeLights) {
            if (!lc.light().prepare(delta)) {
                continue;
            }

            this.lightDepthFb.beginWrite(true);
            RenderSystem.clear(GL31.GL_DEPTH_BUFFER_BIT | GL31.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            MinecraftClientExt.from(this.mc).setFramebuffer(this.lightDepthFb);

            mv.push();

            var lightSource = new LightSource(Objects.requireNonNull(this.mc.world), lc.light());
            this.mc.cameraEntity = lightSource;
            RenderSystem.disableCull();

            try {
                GameRendererExt.from(this.mc.gameRenderer).setActiveRenderLight(lc);
                this.mc.gameRenderer.renderWorld(delta, nanoTime, new MatrixStack());
            } finally {
                GameRendererExt.from(this.mc.gameRenderer).setActiveRenderLight(null);
            }

            this.blitDepthToTex(this.lightDepthFb, lc.depthTex());

            mv.pop();
        }

        MinecraftClientExt.from(this.mc).setFramebuffer(this.target);
        this.target.beginWrite(true);

        this.mc.cameraEntity = oldCam;
        this.mc.options.hudHidden = oldHudHidden;
        this.mc.options.setPerspective(oldPerspective);
    }

    /**
     * Applies the shader onto the target framebuffer
     */
    public void paintSurfaces(float delta, MatrixStack mv, MatrixStack p) {
        if (this.activeLights.isEmpty()) {
            return;
        }

        LightingShader shader = IlluminateClient.instance().shaders.lighting();

        if (shader == null) {
            return;
        }

        Camera camera = this.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        this.blitDepthToTex(this.target, this.playerCamDepthTex());

        RenderSystem.clear(GL31.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

        mv.push();
        mv.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        var lightFb = this.paintSurfaces(mv.peek().getPositionMatrix(), p.peek().getPositionMatrix());
        mv.pop();

        SimpleFramebuffer outFb = lightFb == this.offscreenFb ? this.offscreenFb2 : this.offscreenFb;
        this.compose(this.target, lightFb, outFb);
        this.blit(outFb, this.target);

        this.target.beginWrite(true);
    }

    private Framebuffer paintSurfaces(Matrix4f mv, Matrix4f p) {
        Framebuffer from = this.offscreenFb;
        Framebuffer into = this.offscreenFb2;

        from.clear(MinecraftClient.IS_SYSTEM_MAC);

        LightingShader shader = IlluminateClient.instance().shaders.lighting();

        if (shader == null) {
            throw new IllegalStateException("shader == null");
        }

        GlStateManager._glUseProgram(shader.id());

        RenderSystem.activeTexture(GL31.GL_TEXTURE2);
        RenderSystem.enableTexture();
        RenderSystem.bindTexture(this.playerCamDepthTex());
        RenderSystem.glUniform1i(shader.depth.index(), 2);

        MAT_BUF.clear();
        var mvp = p.mul(mv, new Matrix4f());
        mvp.invert();
        mvp.get(MAT_BUF);
        MAT_BUF.rewind();
        RenderSystem.glUniformMatrix4(shader.camInv.index(), false, MAT_BUF);

        var state = new LoadLightState();

        for (LightContainer l : this.activeLights) {
            if (!this.loadLight(state, l)) {
                this.paintSurfacesPartial(from, into, mv, p, state);

                var tmp = from;
                from = into;
                into = tmp;
                state = new LoadLightState();

                if (!this.loadLight(state, l)) {
                    throw new IllegalStateException("can't add light after drawing!");
                }
            }
        }

        if (state.lightCount > 0) {
            this.paintSurfacesPartial(from, into, mv, p, state);
        }

        GlStateManager._glUseProgram(0);

        RenderSystem.activeTexture(GL31.GL_TEXTURE0);

        return into;
    }

    private void paintSurfacesPartial(Framebuffer from, Framebuffer into, Matrix4f mv, Matrix4f p, LoadLightState state) {
        LightingShader shader = IlluminateClient.instance().shaders.lighting();

        RenderSystem.glUniform1i(shader.lightCount.index(), state.lightCount);

        MAT_BUF.clear();
        ortho(0f, into.textureWidth, 0f, into.textureHeight, -1f, 1f).get(MAT_BUF);
        MAT_BUF.rewind();
        RenderSystem.glUniformMatrix4(shader.mvp.index(), true, MAT_BUF);

        RenderSystem.glUniform1i(shader.width.index(), from.textureWidth);
        RenderSystem.glUniform1i(shader.height.index(), from.textureHeight);

        RenderSystem.activeTexture(GL31.GL_TEXTURE0);
        RenderSystem.enableTexture();
        from.beginRead();
        RenderSystem.glUniform1i(shader.accum.index(), 0);

        RenderSystem.viewport(0, 0, into.textureWidth, into.textureHeight);

        into.clear(MinecraftClient.IS_SYSTEM_MAC);
        into.beginWrite(false);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);

        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(0.0, 0.0, 0.0).texture(0, 0).next();
        buf.vertex(into.textureWidth, 0.0, 0.0).texture(1, 0).next();
        buf.vertex(into.textureWidth, into.textureHeight, 0.0).texture(1, 1).next();
        buf.vertex(0.0, into.textureHeight, 0.0).texture(0, 1).next();
        BufferRenderer.draw(buf.end());

        into.endWrite();
        from.endRead();

        for (int i = 0; i < state.nextTexture; i += 1) {
            RenderSystem.activeTexture(GL31.GL_TEXTURE3 + i);
            RenderSystem.disableTexture();
        }
    }

    private final class LoadLightState {
        int nextTexture = 0;
        int lightCount = 0;
        Identifier[] usedTextures = new Identifier[PostProcess.this.getMaxTextures()];
    }

    private boolean loadLight(LoadLightState state, LightContainer l) {
        LightingShader shader = IlluminateClient.instance().shaders.lighting();

        int color;

        for (color = 0; color < state.nextTexture; color += 1) {
            if (l.light().tex().equals(state.usedTextures[color])) {
                break;
            }
        }

        if (color == state.nextTexture) {
            if (state.nextTexture + 2 >= state.usedTextures.length) {
                return false;
            }

            RenderSystem.activeTexture(GL31.GL_TEXTURE3 + color);
            RenderSystem.enableTexture();
            this.mc.getTextureManager().bindTexture(l.light().tex());
            state.usedTextures[color] = l.light().tex();
            state.nextTexture += 1;
        } else {
            if (state.nextTexture + 1 >= state.usedTextures.length) {
                return false;
            }
        }

        int depth = state.nextTexture;
        RenderSystem.activeTexture(GL31.GL_TEXTURE3 + depth);
        RenderSystem.enableTexture();
        RenderSystem.bindTexture(l.depthTex());
        state.nextTexture += 1;

        RenderSystem.glUniform1i(shader.lightTex.index(state.lightCount), color);
        RenderSystem.glUniform1i(shader.lightDepth.index(state.lightCount), depth);

        MAT_BUF.clear();
        l.mvp().get(MAT_BUF);
        MAT_BUF.rewind();
        RenderSystem.glUniformMatrix4(shader.lightCam.index(state.lightCount), false, MAT_BUF);

        Vector3fc lightPos = l.light().pos();
        GL31.glUniform3f(shader.lightPos.index(state.lightCount), lightPos.x(), lightPos.y(), lightPos.z());

        state.lightCount += 1;

        return true;
    }

    private void compose(Framebuffer color, Framebuffer light, Framebuffer into) {
        ComposeShader shader = IlluminateClient.instance().shaders.compose();

        GlStateManager._glUseProgram(shader.id());

        new Matrix4f().get(MAT_BUF);
        GlStateManager._glUniformMatrix4(shader.mvp.index(), false, MAT_BUF);

        GlStateManager._activeTexture(GL31.GL_TEXTURE0);
        GlStateManager._enableTexture();
        color.beginRead();
        GlStateManager._glUniform1i(shader.worldTex.index(), 0);

        GlStateManager._activeTexture(GL31.GL_TEXTURE2);
        GlStateManager._enableTexture();
        light.beginRead();
        GlStateManager._glUniform1i(shader.lightTex.index(), 2);

        RenderSystem.viewport(0, 0, into.textureWidth, into.textureHeight);

        into.clear(MinecraftClient.IS_SYSTEM_MAC);
        into.beginWrite(false);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);

        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(-1.0, -1.0, 0.0).texture(0, 0).next();
        buf.vertex(1.0, -1.0, 0.0).texture(1, 0).next();
        buf.vertex(1.0, 1.0, 0.0).texture(1, 1).next();
        buf.vertex(-1.0, 1.0, 0.0).texture(0, 1).next();
        BufferRenderer.draw(buf.end());

        into.endWrite();

        GlStateManager._activeTexture(GL31.GL_TEXTURE2);
        light.endRead();

        GlStateManager._activeTexture(GL31.GL_TEXTURE0);
        color.endRead();

        GlStateManager._glUseProgram(0);
    }

    private void blit(Framebuffer from, Framebuffer into) {
        if (from == into) {
            return;
        }

        GlStateManager._glBindFramebuffer(GL31.GL_READ_FRAMEBUFFER, from.fbo);
        GlStateManager._glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, into.fbo);
        GlStateManager._glBlitFrameBuffer(
                0,
                0,
                from.textureWidth,
                from.textureHeight,
                0,
                0,
                into.textureWidth,
                into.textureHeight,
                GL31.GL_COLOR_BUFFER_BIT | GL31.GL_DEPTH_BUFFER_BIT,
                GL31.GL_NEAREST
        );
        GlStateManager._glBindFramebuffer(GL31.GL_READ_FRAMEBUFFER, 0);
        GlStateManager._glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, 0);
    }

    private void blitDepthToTex(Framebuffer from, int into) {
        RenderSystem.bindTexture(into);
        GL31.glTexImage2D(
                GL31.GL_TEXTURE_2D,
                0,
                GL31.GL_DEPTH_COMPONENT,
                from.textureWidth,
                from.textureHeight,
                0,
                GL31.GL_DEPTH_COMPONENT,
                GL31.GL_FLOAT,
                0
        );

        GlStateManager._glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, this.blitFb());
        GlStateManager._glFramebufferTexture2D(
                GL31.GL_DRAW_FRAMEBUFFER,
                GL31.GL_DEPTH_ATTACHMENT,
                GL31.GL_TEXTURE_2D,
                into,
                0
        );

        GlStateManager._glBindFramebuffer(GL31.GL_READ_FRAMEBUFFER, from.fbo);

        GlStateManager._glBlitFrameBuffer(
                0,
                0,
                from.textureWidth,
                from.textureHeight,
                0,
                0,
                from.textureWidth,
                from.textureHeight,
                GL31.GL_DEPTH_BUFFER_BIT,
                GL31.GL_NEAREST
        );

        GlStateManager._glBindFramebuffer(GL31.GL_READ_FRAMEBUFFER, 0);
        GlStateManager._glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, 0);
        RenderSystem.bindTexture(0);
    }

    public void destroy() {
        if (this.playerCamDepthTex != 0) {
            RenderSystem.deleteTexture(this.playerCamDepthTex);
            this.playerCamDepthTex = 0;
        }

        if (this.blitFb != 0) {
            GlStateManager._glDeleteFramebuffers(this.blitFb);
            this.blitFb = 0;
        }

        this.offscreenFb.delete();
        this.offscreenFb2.delete();
        this.lightDepthFb.delete();
        this.lights.values().forEach(LightContainer::destroy);
        this.lights.clear();
    }

    private static Matrix4f ortho(float left, float right, float bottom, float top, float zNear, float zFar) {
        var m00 = 2f / (right - left);
        var m11 = 2f / (top - bottom);
        var m22 = -2f / (zFar - zNear);
        var m03 = -(right + left) / (right - left);
        var m13 = -(top + bottom) / (top - bottom);
        var m23 = -(zFar + zNear) / (zFar - zNear);

        return new Matrix4f(
                m00, 0f, 0f, m03,
                0f, m11, 0f, m13,
                0f, 0f, m22, m23,
                0f, 0f, 0f, 1f
        );
    }

    public void onJoinWorld() {
        this.lights.clear();
    }
}
