package net.dblsaiko.illuminate.test;

import net.dblsaiko.illuminate.test.init.Blocks;
import net.dblsaiko.illuminate.init.EntityTypes;
import net.dblsaiko.illuminate.test.init.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IlluminateTest {
    public static final String MOD_ID = "illuminate-test";

    private static IlluminateTest instance;

    public final Blocks blocks = new Blocks();
    public final Items items = new Items(this.blocks);

    private IlluminateTest() {
    }

    public static void initialize() {
        if (instance != null) {
            throw new IllegalStateException("IlluminateTest::initialize called twice!");
        }

        IlluminateTest it = new IlluminateTest();
        it.init();
        instance = it;
    }

    private void init() {
        this.blocks.register();
        this.items.register();
    }

    @NotNull
    public static IlluminateTest instance() {
        return Objects.requireNonNull(instance);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
