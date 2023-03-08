package net.dblsaiko.illuminate.test.init;

import net.dblsaiko.illuminate.test.IlluminateTest;
import net.dblsaiko.illuminate.test.block.LightBlock;
import net.dblsaiko.illuminate.test.block.ProjectorBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class Blocks {
    public final ProjectorBlock projector = new ProjectorBlock(AbstractBlock.Settings.of(Material.METAL));
    public final LightBlock light = new LightBlock(AbstractBlock.Settings.of(Material.WOOL));

    public void register() {
        Registry.register(Registries.BLOCK, IlluminateTest.id("projector"), this.projector);
        Registry.register(Registries.BLOCK, IlluminateTest.id("light"), this.light);
    }
}
