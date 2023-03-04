package net.dblsaiko.illuminate.test;

import net.dblsaiko.illuminate.client.IlluminateClient;
import net.dblsaiko.illuminate.client.api.Light;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class CreeperLight implements Light {
    private final Entity e;

    private final Vector3f posBuf = new Vector3f();

    private float delta;

    public CreeperLight(Entity e) {
        this.e = e;
    }

    @Override
    @NotNull
    public Identifier tex() {
        return BlockLight.TEXTURE;
    }

    @Override
    @NotNull
    public Vector3fc pos() {
        var pos = this.e.getCameraPosVec(this.delta);
        this.posBuf.set(pos.x, pos.y, pos.z);
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
        return 40f;
    }

    @Override
    public boolean prepare(float delta) {
        this.delta = delta;

        if (this.e.isRemoved() || this.e.world != MinecraftClient.getInstance().world) {
            IlluminateClient.instance().removeLight(this);
            return false;
        }

        return true;
    }
}
