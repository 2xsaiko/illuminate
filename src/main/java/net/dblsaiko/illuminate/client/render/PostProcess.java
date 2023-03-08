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
import org.joml.Vector3f;
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

    private int uMvp = 0;
    private int uCamInv = 0;
    private int uWidth = 0;
    private int uHeight = 0;
    private int uWorld = 0;
    private int uDepth = 0;
    private final int[] uTexTable;
    private final int[] uLightTex;
    private final int[] uLightDepth;
    private final int[] uLightCam;
    private final int[] uLightPos;
    private int uLightCount = 0;

    private final SimpleFramebuffer offscreenFb;
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

        this.uTexTable = new int[this.getMaxTextures()];
        this.uLightTex = new int[this.getMaxTextures()];
        this.uLightDepth = new int[this.getMaxTextures()];
        this.uLightCam = new int[this.getMaxTextures()];
        this.uLightPos = new int[this.getMaxTextures()];
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
        int shader = IlluminateClient.instance().shaders.lighting();

        if (shader > 0) {
            GlStateManager._glUseProgram(shader);

            this.uMvp = GlStateManager._glGetUniformLocation(shader, "mvp");
            this.uCamInv = GlStateManager._glGetUniformLocation(shader, "camInv");
            this.uWidth = GlStateManager._glGetUniformLocation(shader, "width");
            this.uHeight = GlStateManager._glGetUniformLocation(shader, "height");
            this.uWorld = GlStateManager._glGetUniformLocation(shader, "world");
            this.uDepth = GlStateManager._glGetUniformLocation(shader, "depth");

            for (int i = 0; i < this.getMaxTextures(); i += 1) {
                this.uTexTable[i] = GlStateManager._glGetUniformLocation(shader, "texTable[%d]".formatted(i));
                this.uLightTex[i] = GlStateManager._glGetUniformLocation(shader, "lightTex[%d]".formatted(i));
                this.uLightDepth[i] = GlStateManager._glGetUniformLocation(shader, "lightDepth[%d]".formatted(i));
                this.uLightCam[i] = GlStateManager._glGetUniformLocation(shader, "lightCam[%d]".formatted(i));
                this.uLightPos[i] = GlStateManager._glGetUniformLocation(shader, "lightPos[%d]".formatted(i));

                // These always have the same values, so set it here
                RenderSystem.glUniform1i(this.uTexTable[i], 3 + i);
            }

            this.uLightCount = GlStateManager._glGetUniformLocation(shader, "lightCount");

            GlStateManager._glUseProgram(0);
        }
    }

    /**
     * Determine which lights should be rendered from the player's position
     */
    public void setupLights(float delta) {
        this.activeLights.clear();

        Vector3f camPos = this.mc.gameRenderer.getCamera().getPos().toVector3f();

        var closestLights = new ArrayList<>(this.lights.values());
        closestLights.sort(Comparator.comparingDouble(el -> el.light().pos().sub(camPos, new Vector3f()).lengthSquared()));

        var usedTex = new HashSet<Identifier>();

        for (var light : closestLights) {
            usedTex.add(light.light().tex());

            // we need 1 texture ID for each distinct light texture + 1 texture
            // ID for each light's depth buffer
            if (usedTex.size() + this.activeLights.size() + 1 > this.getMaxTextures()) {
                break;
            }

            this.activeLights.add(light);
        }
    }

    /**
     * Resize the framebuffers and textures to the specified size.
     */
    public void resize(int width, int height) {
        this.offscreenFb.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
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

        int shader = IlluminateClient.instance().shaders.lighting();

        if (shader == 0) {
            return;
        }

        Camera camera = this.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        this.blitDepthToTex(this.target, this.playerCamDepthTex());

        RenderSystem.clear(GL31.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

        mv.push();
        mv.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        this.paintSurfaces(this.target, this.offscreenFb, mv.peek().getPositionMatrix(), p.peek().getPositionMatrix());
        mv.pop();
        this.blit(this.offscreenFb, this.target);

        this.target.beginWrite(true);
    }

    private void paintSurfaces(Framebuffer from, Framebuffer into, Matrix4f mv, Matrix4f p) {
        int shader = IlluminateClient.instance().shaders.lighting();

        if (shader == 0) {
            throw new IllegalStateException("shader == 0");
        }

        from.endWrite();

        GlStateManager._glUseProgram(shader);

        RenderSystem.glUniform1i(this.uWidth, from.textureWidth);
        RenderSystem.glUniform1i(this.uHeight, from.textureHeight);

        RenderSystem.activeTexture(GL31.GL_TEXTURE0);
        RenderSystem.enableTexture();
        from.beginRead();
        RenderSystem.glUniform1i(this.uWorld, 0);

        RenderSystem.activeTexture(GL31.GL_TEXTURE2);
        RenderSystem.enableTexture();
        RenderSystem.bindTexture(this.playerCamDepthTex());
        RenderSystem.glUniform1i(this.uDepth, 2);
        RenderSystem.activeTexture(GL31.GL_TEXTURE0);

        var state = new LoadLightState();

        for (LightContainer l : this.activeLights) {
            this.loadLight(state, l);
        }

        RenderSystem.glUniform1i(this.uLightCount, state.lightCount);

        MAT_BUF.clear();
        ortho(0f, into.textureWidth, into.textureHeight, 0f, -1f, 1f).get(MAT_BUF);
        MAT_BUF.rewind();
        RenderSystem.glUniformMatrix4(this.uMvp, true, MAT_BUF);

        MAT_BUF.clear();
        var mvp = p.mul(mv, new Matrix4f());
        mvp.invert();
        mvp.get(MAT_BUF);
        MAT_BUF.rewind();
        RenderSystem.glUniformMatrix4(this.uCamInv, false, MAT_BUF);

        RenderSystem.viewport(0, 0, into.textureWidth, into.textureHeight);

        into.clear(MinecraftClient.IS_SYSTEM_MAC);
        into.beginWrite(false);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);

        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buf.vertex(0.0, 0.0, 0.0).next();
        buf.vertex(0.0, into.textureHeight, 0.0).next();
        buf.vertex(into.textureWidth, into.textureHeight, 0.0).next();
        buf.vertex(into.textureWidth, 0.0, 0.0).next();
        BufferRenderer.draw(buf.end());

        into.endWrite();
        GlStateManager._glUseProgram(0);

        from.endRead();

        for (int i = 0; i < state.nextTexture; i += 1) {
            RenderSystem.activeTexture(GL31.GL_TEXTURE3 + i);
            RenderSystem.disableTexture();
        }

        RenderSystem.activeTexture(GL31.GL_TEXTURE0);
    }

    private final class LoadLightState {
        int nextTexture = 0;
        int lightCount = 0;
        Identifier[] usedTextures = new Identifier[PostProcess.this.getMaxTextures()];
    }

    private void loadLight(LoadLightState state, LightContainer l) {
        int color;

        for (color = 0; color < state.nextTexture; color += 1) {
            if (l.light().tex().equals(state.usedTextures[color])) {
                break;
            }
        }

        if (color == state.nextTexture) {
            RenderSystem.activeTexture(GL31.GL_TEXTURE3 + color);
            RenderSystem.enableTexture();
            this.mc.getTextureManager().bindTexture(l.light().tex());
            state.usedTextures[color] = l.light().tex();
            state.nextTexture += 1;
        }

        int depth = state.nextTexture;
        RenderSystem.activeTexture(GL31.GL_TEXTURE3 + depth);
        RenderSystem.enableTexture();
        RenderSystem.bindTexture(l.depthTex());
        state.nextTexture += 1;

        RenderSystem.glUniform1i(this.uLightTex[state.lightCount], color);
        RenderSystem.glUniform1i(this.uLightDepth[state.lightCount], depth);

        MAT_BUF.clear();
        l.mvp().get(MAT_BUF);
        MAT_BUF.rewind();
        RenderSystem.glUniformMatrix4(this.uLightCam[state.lightCount], false, MAT_BUF);

        Vector3fc lightPos = l.light().pos();
        GL31.glUniform3f(this.uLightPos[state.lightCount], lightPos.x(), lightPos.y(), lightPos.z());

        state.lightCount += 1;
    }

    private void blit(Framebuffer from, Framebuffer into) {
        GlStateManager._glBindFramebuffer(GL31.GL_READ_FRAMEBUFFER, from.fbo);
        GlStateManager._glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, into.fbo);
        GlStateManager._glBlitFrameBuffer(
                0,
                0,
                from.textureWidth,
                from.textureHeight,
                0,
                into.textureHeight,
                into.textureWidth,
                0,
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
