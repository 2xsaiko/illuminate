package net.dblsaiko.illuminate.client.util;

import com.mojang.blaze3d.platform.GlStateManager;

public class ScalarUniform implements Uniform {
    private final Program program;
    private final String name;

    private int index;

    ScalarUniform(Program program, String name) {
        this.program = program;
        this.name = name;
    }

    public int index() {
        return this.index;
    }

    @Override
    public Program program() {
        return this.program;
    }

    @Override
    public void setup() {
        this.index = GlStateManager._glGetUniformLocation(this.program.id(), this.name);
    }
}
