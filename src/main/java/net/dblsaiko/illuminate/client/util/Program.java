package net.dblsaiko.illuminate.client.util;

import com.mojang.blaze3d.platform.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class Program {
    private final int id;
    private final List<Uniform> uniforms = new ArrayList<>();
    private boolean setup = false;

    public Program(int id) {
        this.id = id;
    }

    public final void setup() {
        if (this.setup) {
            throw new IllegalStateException("Shader is locked!");
        }

        // Despite glGetUniformLocation taking a shader ID, not doing this
        // before getting uniform IDs causes a "shader not linked" error.
        GlStateManager._glUseProgram(this.id);

        for (Uniform uniform : this.uniforms) {
            uniform.setup();
        }

        this.setup = true;
    }

    public final ScalarUniform uniform(String name) {
        if (this.setup) {
            throw new IllegalStateException("Shader is locked!");
        }

        ScalarUniform v = new ScalarUniform(this, name);
        this.uniforms.add(v);
        return v;
    }

    public final UniformArray uniformArray(String name, int len) {
        if (this.setup) {
            throw new IllegalStateException("Shader is locked!");
        }

        UniformArray v = new UniformArray(this, name, len);
        this.uniforms.add(v);
        return v;
    }

    public final int id() {
        return this.id;
    }
}
