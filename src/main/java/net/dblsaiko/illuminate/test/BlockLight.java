package net.dblsaiko.illuminate.test;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dblsaiko.illuminate.client.IlluminateClient;
import net.dblsaiko.illuminate.client.api.Light;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BlockLight implements Light {
    private static final float Z_NEAR = (float) (Math.sqrt(2f) / 2);

    private final World world;
    private final BlockPos pos;

    private final Vector3f posBuf = new Vector3f();
    private boolean wait = true;
    private float yaw;

    public BlockLight(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public int tex() {
        return getTex();
    }

    @Override
    public Vector3fc pos() {
        this.posBuf.set(this.pos.getX() + 0.5f, this.pos.getY() + 0.5f, this.pos.getZ() + 0.5f);
        return this.posBuf;
    }

    @Override
    public float yaw() {
        return this.yaw;
    }

    @Override
    public float fov() {
        return 35f;
    }

    @Override
    public float aspect() {
        return 768 / 576f;
    }

    @Override
    public float near() {
        return Z_NEAR;
    }

    @Override
    public void prepare(float delta) {
        BlockState state = this.world.getBlockState(this.pos);

        if (state.getBlock() != IlluminateTest.instance().blocks.projector) {
            if (!this.wait) {
                IlluminateClient.instance().removeLight(this);
            }

            return;
        } else {
            this.wait = false;
        }

        this.yaw = state.get(Properties.HORIZONTAL_FACING).asRotation();
    }

    public static int getTex() {
        TextureManager tm = MinecraftClient.getInstance().getTextureManager();
        Identifier id = IlluminateTest.id("textures/test.png");
        tm.bindTexture(id);
        RenderSystem.bindTexture(0);

        return tm.getTexture(id).getGlId();
    }
}
