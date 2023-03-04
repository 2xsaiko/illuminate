package net.dblsaiko.illuminate.test;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ProjectorBlockLight extends BlockLight {
    private static final float Z_NEAR = (float) (Math.sqrt(2f) / 2);
    public static final Identifier TEXTURE = IlluminateTest.id("textures/test.png");

    private float yaw;

    public ProjectorBlockLight(World world, BlockPos pos) {
        super(world, pos, IlluminateTest.instance().blocks.projector);
    }

    @Override
    @NotNull
    public Identifier tex() {
        return TEXTURE;
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
    public boolean prepare(float delta) {
        if (!super.prepare(delta)) {
            return false;
        }

        BlockState state = this.world.getBlockState(this.pos);
        this.yaw = state.get(Properties.HORIZONTAL_FACING).asRotation();
        return true;
    }
}
