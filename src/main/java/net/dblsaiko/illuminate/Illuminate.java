package net.dblsaiko.illuminate;

import net.minecraft.util.Identifier;

public class Illuminate {
    public static final String MOD_ID = "illuminate";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
