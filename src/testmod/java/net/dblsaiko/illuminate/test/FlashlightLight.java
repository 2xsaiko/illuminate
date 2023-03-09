package net.dblsaiko.illuminate.test;

import net.dblsaiko.illuminate.client.IlluminateClient;
import net.dblsaiko.illuminate.client.api.Light;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class FlashlightLight implements Light {
    public static final Identifier TEXTURE = IlluminateTest.id("textures/flashlight.png");

    private final LivingEntity e;

    private final Vector3f posBuf = new Vector3f();

    private float delta;

    public FlashlightLight(LivingEntity e) {
        this.e = e;
    }

    @Override
    @NotNull
    public Identifier tex() {
        return TEXTURE;
    }

    @Override
    @NotNull
    public Vector3fc pos() {
        var pos = this.e.getCameraPosVec(this.delta);
        this.posBuf.set(pos.x, pos.y - 0.25f, pos.z);
        return this.posBuf;
    }

    @Override
    public float yaw() {
        return this.e.getYaw(this.delta);
    }

    @Override
    public float pitch() {
        return this.e.getPitch(this.delta);
    }

    @Override
    public float fov() {
        return 45f;
    }

    @Override
    public float brightness() {
        return 64;
    }

    @Override
    public boolean selfPlayerShadow() {
        return false;
    }

    @Override
    public boolean prepare(float delta) {
        this.delta = delta;

        if (this.e.isRemoved() || this.e.world != MinecraftClient.getInstance().world || this.e.getMainHandStack().getItem() != IlluminateTest.instance().items.flashlight) {
            IlluminateClient.instance().removeLight(this);
            return false;
        }

        return true;
    }
}
