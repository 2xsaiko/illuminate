package net.dblsaiko.illuminate.test;

import net.dblsaiko.illuminate.client.IlluminateClient;
import net.dblsaiko.illuminate.client.api.Light;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public abstract class BlockLight implements Light {
    protected final World world;
    protected final BlockPos pos;
    protected final Block block;

    private final Vector3f posBuf = new Vector3f();
    private boolean wait = true;

    public BlockLight(World world, BlockPos pos, Block block) {
        this.world = world;
        this.pos = pos;
        this.block = block;
    }

    @Override
    @NotNull
    public Vector3fc pos() {
        this.posBuf.set(this.pos.getX() + 0.5f, this.pos.getY() + 0.5f, this.pos.getZ() + 0.5f);
        return this.posBuf;
    }

    @Override
    public boolean prepare(float delta) {
        BlockState state = this.world.getBlockState(this.pos);

        if (state.getBlock() != this.block) {
            if (!this.wait) {
                IlluminateClient.instance().removeLight(this);
            }

            return false;
        } else {
            this.wait = false;
        }

        return true;
    }
}
