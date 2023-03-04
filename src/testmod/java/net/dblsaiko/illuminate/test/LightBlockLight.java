package net.dblsaiko.illuminate.test;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class LightBlockLight extends BlockLight {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/block/white_wool.png");

    private final Direction facing;

    public LightBlockLight(World world, BlockPos pos, Direction facing) {
        super(world, pos, IlluminateTest.instance().blocks.light);
        this.facing = facing;
    }

    @Override
    @NotNull
    public Identifier tex() {
        return TEXTURE;
    }

    @Override
    public float yaw() {
        return switch (this.facing) {
            case DOWN -> 0.0F;
            case UP -> 0.0F;
            default -> this.facing.asRotation();
        };
    }

    @Override
    public float pitch() {
        return switch (this.facing) {
            case DOWN -> -90.0F;
            case UP -> 90.0F;
            default -> 0.0F;
        };
    }
}
