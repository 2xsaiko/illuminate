package net.dblsaiko.illuminate;

import net.dblsaiko.illuminate.init.EntityTypes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Illuminate {
    public static final String MOD_ID = "illuminate";

    private static Illuminate instance;

    public final EntityTypes entityTypes = new EntityTypes();

    private Illuminate() {
    }

    public static void initialize() {
        if (instance != null) {
            throw new IllegalStateException("Illuminate::initialize called twice!");
        }

        Illuminate i = new Illuminate();
        i.init();
        instance = i;
    }

    private void init() {
        this.entityTypes.register();
    }

    @NotNull
    public static Illuminate instance() {
        return Objects.requireNonNull(instance);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
