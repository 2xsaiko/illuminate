package net.dblsaiko.illuminate.client;

import net.dblsaiko.illuminate.client.api.Light;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.joml.Vector3fc;
import therealfarfetchd.illuminate.Illuminate;

public class LightSource extends Entity {
    public LightSource(World world, Light light) {
        super(Illuminate.INSTANCE.getLightEntityType(), world);

        Vector3fc lightPos = light.pos();
        this.setPosition(lightPos.x(), lightPos.y(), lightPos.z());
        this.setRotation(light.yaw(), light.pitch());

        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        this.prevPitch = this.getPitch();
        this.prevYaw = this.getYaw();
        this.lastRenderX = this.getX();
        this.lastRenderY = this.getX();
        this.lastRenderZ = this.getZ();

    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }
}
