package net.dblsaiko.illuminate.client.util;

import com.mojang.blaze3d.platform.GlStateManager;

public class UniformArray implements Uniform {
    private final Program program;
    private final String name;

    private final int[] indices;

    UniformArray(Program program, String name, int len) {
        this.program = program;
        this.name = name;
        this.indices = new int[len];
    }

    public int length() {
        return this.indices.length;
    }

    public int index(int idx) {
        return this.indices[idx];
    }

    @Override
    public Program program() {
        return this.program;
    }

    @Override
    public void setup() {
        for (int i = 0; i < this.indices.length; i++) {
            this.indices[i] = GlStateManager._glGetUniformLocation(this.program.id(), "%s[%d]".formatted(this.name, i));
        }
    }
}
