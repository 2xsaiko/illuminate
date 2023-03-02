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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL31;

import java.nio.FloatBuffer;
import java.util.*;

public class PostProcess {
    // Apple can suck my ass
    // TODO: get max samplers from GL
    private static final int CAM_LIMIT = MinecraftClient.IS_SYSTEM_MAC ? 7 : 10;

    private static final FloatBuffer MAT_BUF = BufferUtils.createFloatBuffer(16);

    private final MinecraftClient mc;
    private final Framebuffer target;

    private int uMvp = 0;
    private int uCamInv = 0;
    private int uWidth = 0;
    private int uHeight = 0;
    private int uWorld = 0;
    private int uDepth = 0;
    private final int[] uLightTex = new int[CAM_LIMIT];
    private final int[] uLightDepth = new int[CAM_LIMIT];
    private final int[] uLightCam = new int[CAM_LIMIT];
    private final int[] uLightPos = new int[CAM_LIMIT];
    private int uLightCount = 0;

    private final SimpleFramebuffer offscreenFb;
    private final LightFramebuffer lightDepthFb = new LightFramebuffer(1024, 1024, MinecraftClient.IS_SYSTEM_MAC);
    private final int blitFb;

    private final int playerCamDepthTex;

    private final Map<Light, LightContainer> lights = new HashMap<>();
    private final Set<LightContainer> activeLights = new HashSet<>();
    private final Set<LightContainer> activeLightsView = Collections.unmodifiableSet(this.activeLights);

    public PostProcess(MinecraftClient mc) {
        this.mc = mc;
        this.target = mc.getFramebuffer();
        this.offscreenFb = new SimpleFramebuffer(this.target.viewportWidth, this.target.viewportHeight, true, MinecraftClient.IS_SYSTEM_MAC);

        // FIXME: do this on first use, not in the constructor
        this.playerCamDepthTex = GlStateManager._genTexture();
        RenderSystem.bindTexture(this.playerCamDepthTex);
        RenderSystem.texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_NEAREST);
        RenderSystem.texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_NEAREST);
        RenderSystem.texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_S, GL31.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_T, GL31.GL_CLAMP_TO_EDGE);

        // FIXME: do this on first use, not in the constructor
        this.blitFb = GlStateManager.glGenFramebuffers();
        // TODO: what was this needed for?
        GlStateManager._glBindFramebuffer(GL31.GL_FRAMEBUFFER, this.blitFb);
        GL31.glDrawBuffer(GL31.GL_NONE);
        this.target.beginWrite(false);
    }

    public int getMaxLights() {
        return CAM_LIMIT;
    }

    public int playerCamDepthTex() {
        return this.playerCamDepthTex;
    }

    public boolean addLight(Light light) {
        if (this.lights.containsKey(light)) {
            return false;
        }

        this.lights.put(light, new LightContainer(light));
        return true;
    }

    public boolean removeLight(Light light) {
        return this.lights.remove(light) != null;
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

            for (int i = 0; i < CAM_LIMIT; i += 1) {
                this.uLightTex[i] = GlStateManager._glGetUniformLocation(shader, "lightTex[%d]".formatted(i));
                this.uLightDepth[i] = GlStateManager._glGetUniformLocation(shader, "lightDepth[%d]".formatted(i));
                this.uLightCam[i] = GlStateManager._glGetUniformLocation(shader, "lightCam[%d]".formatted(i));
                this.uLightPos[i] = GlStateManager._glGetUniformLocation(shader, "lightPos[%d]".formatted(i));
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

        if (this.lights.size() < CAM_LIMIT) {
            this.activeLights.addAll(this.lights.values());
            return;
        }

        Vector3f camPos = this.mc.gameRenderer.getCamera().getPos().toVector3f();
        this.lights.values()
                .stream()
                .sorted(Comparator.comparingDouble(el -> el.light().pos().sub(camPos, new Vector3f()).lengthSquared()))
                .limit(CAM_LIMIT)
                .forEach(this.activeLights::add);
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
            this.lightDepthFb.beginWrite(true);
            RenderSystem.clear(GL31.GL_DEPTH_BUFFER_BIT | GL31.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            MinecraftClientExt.from(this.mc).setFramebuffer(this.lightDepthFb);

            mv.push();
            lc.light().prepare(delta);

            // this.setupCamera(lc);

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

        this.blitDepthToTex(this.target, this.playerCamDepthTex);

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

        RenderSystem.enableTexture();
        from.beginRead();
        RenderSystem.glUniform1i(this.uWorld, 0);

        RenderSystem.activeTexture(GL31.GL_TEXTURE2);
        RenderSystem.enableTexture();
        RenderSystem.bindTexture(this.playerCamDepthTex);
        RenderSystem.glUniform1i(this.uDepth, 2);
        RenderSystem.activeTexture(GL31.GL_TEXTURE0);

        {
            int i = 0;

            for (LightContainer l : this.activeLights) {
                this.loadLight(i, l);
                i += 1;
            }

            RenderSystem.glUniform1i(this.uLightCount, i);
        }

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

        for (int i = 2; i < 3 + this.activeLights.size() * 2; i += 1) {
            RenderSystem.activeTexture(GL31.GL_TEXTURE0 + i);
            RenderSystem.disableTexture();
        }

        RenderSystem.activeTexture(GL31.GL_TEXTURE0);
    }

    private void loadLight(int i, LightContainer l) {
        RenderSystem.activeTexture(GL31.GL_TEXTURE3 + 2 * i);
        RenderSystem.enableTexture();
        this.mc.getTextureManager().bindTexture(l.light().tex());

        RenderSystem.activeTexture(GL31.GL_TEXTURE4 + 2 * i);
        RenderSystem.enableTexture();
        RenderSystem.bindTexture(l.depthTex());

        RenderSystem.glUniform1i(this.uLightTex[i], 3 + 2 * i);
        RenderSystem.glUniform1i(this.uLightDepth[i], 4 + 2 * i);

        MAT_BUF.clear();
        l.mvp().get(MAT_BUF);
        MAT_BUF.rewind();
        RenderSystem.glUniformMatrix4(this.uLightCam[i], false, MAT_BUF);

        Vector3fc lightPos = l.light().pos();
        GL31.glUniform3f(this.uLightPos[i], lightPos.x(), lightPos.y(), lightPos.z());
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

        GlStateManager._glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, this.blitFb);
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
        RenderSystem.deleteTexture(this.playerCamDepthTex);
        this.offscreenFb.delete();
        this.lightDepthFb.delete();
        GlStateManager._glDeleteFramebuffers(this.blitFb);
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
