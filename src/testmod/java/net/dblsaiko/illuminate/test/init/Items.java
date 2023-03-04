package net.dblsaiko.illuminate.test.init;

import net.dblsaiko.illuminate.test.IlluminateTest;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class Items {
    public final BlockItem projector;
    public final BlockItem light;

    public Items(Blocks blocks) {
        this.projector = new BlockItem(blocks.projector, new Item.Settings());
        this.light = new BlockItem(blocks.light, new Item.Settings());
    }

    public void register() {
        Registry.register(Registries.ITEM, IlluminateTest.id("projector"), this.projector);
        Registry.register(Registries.ITEM, IlluminateTest.id("light"), this.light);
    }
}
