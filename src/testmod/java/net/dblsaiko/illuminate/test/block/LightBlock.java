package net.dblsaiko.illuminate.test.block;

import net.dblsaiko.illuminate.client.IlluminateClient;
import net.dblsaiko.illuminate.test.LightBlockLight;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class LightBlock extends Block {
    public LightBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        // This currently only works in single-player, and also doesn't
        // restore the light when reloading the world.
        // However, this is just a test mod so it's good enough.
        MinecraftClient.getInstance().execute(() -> {
            for (Direction d : Direction.values()) {
                IlluminateClient.instance().addLight(new LightBlockLight(world, pos, d));
            }
        });
    }
}
