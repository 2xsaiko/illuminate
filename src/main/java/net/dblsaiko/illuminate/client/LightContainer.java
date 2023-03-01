package net.dblsaiko.illuminate.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.dblsaiko.illuminate.client.api.Light;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL31;

public class LightContainer {
    private final Light light;
    private int depthTex = 0;

    private final Vector3f cachedPos = new Vector3f();
    private float cachedYaw;
    private float cachedPitch;
    private float cachedRoll;
    private float cachedFov;
    private float cachedAspect;
    private float cachedNear;
    private float cachedFar;

    private final Matrix4f mvp = new Matrix4f();
    private boolean mvpDirty = true;
    private final Matrix4f mv = new Matrix4f();
    private boolean mvDirty = true;
    private final Matrix4f p = new Matrix4f();
    private boolean pDirty = true;

    public LightContainer(Light light) {
        this.light = light;
    }

    public Light light() {
        return this.light;
    }

    public int depthTex() {
        if (this.depthTex == 0) {
            this.depthTex = GlStateManager._genTexture();
            GlStateManager._bindTexture(this.depthTex);
            GlStateManager._texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR);
            GlStateManager._texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR);
            GlStateManager._texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_S, GL31.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_T, GL31.GL_CLAMP_TO_EDGE);
        }

        return this.depthTex;
    }

    public Matrix4fc mvp() {
        this.update();

        Matrix4fc mv = this._mv();
        Matrix4fc p = this._p();

        if (this.mvpDirty) {
            p.mul(mv.translate(this.cachedPos.negate(new Vector3f()), this.mvp), this.mvp);
            this.mvpDirty = false;
        }

        return this.mvp;
    }

    public Matrix4fc mv() {
        this.update();
        return this._mv();
    }

    public Matrix4fc p() {
        this.update();
        return this._p();
    }

    private Matrix4fc _mv() {
        if (this.mvDirty) {
            this.mv.set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
            this.mv.rotate(this.cachedRoll, 0, 0, 1);
            this.mv.rotate(this.cachedPitch, 1, 0, 0);
            this.mv.rotate(this.cachedYaw, 0, 1, 0);
            this.mvDirty = false;
            this.mvpDirty = true;
        }

        return this.mv;
    }

    private Matrix4fc _p() {
        if (this.pDirty) {
            this.p.set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
            this.p.perspective(this.cachedFov, this.cachedAspect, this.cachedNear, this.cachedFar);
            this.pDirty = false;
            this.mvpDirty = true;
        }

        return this.p;
    }

    public void update() {
        Vector3fc pos = this.light.pos();
        float yaw = this.light.yaw();
        float pitch = this.light.pitch();
        float roll = this.light.roll();
        float fov = this.light.fov();
        float aspect = this.light.aspect();
        float near = this.light.near();
        float far = this.light.far();

        if (!this.cachedPos.equals(pos)) {
            this.cachedPos.set(pos);
            this.mvpDirty = true;
        }

        if (this.cachedYaw != yaw) {
            this.cachedYaw = yaw;
            this.mvDirty = true;
        }

        if (this.cachedPitch != pitch) {
            this.cachedPitch = pitch;
            this.mvDirty = true;
        }

        if (this.cachedRoll != roll) {
            this.cachedRoll = roll;
            this.mvDirty = true;
        }

        if (this.cachedFov != fov) {
            this.cachedFov = fov;
            this.pDirty = true;
        }

        if (this.cachedAspect != aspect) {
            this.cachedAspect = aspect;
            this.pDirty = true;
        }

        if (this.cachedNear != near) {
            this.cachedNear = near;
            this.pDirty = true;
        }

        if (this.cachedFar != far) {
            this.cachedFar = far;
            this.pDirty = true;
        }
    }

    public void destroy() {
        if (this.depthTex != 0) {
            GlStateManager._deleteTexture(this.depthTex);
        }

        this.depthTex = 0;
    }
}
