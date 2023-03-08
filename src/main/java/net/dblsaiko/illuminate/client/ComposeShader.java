package net.dblsaiko.illuminate.client;

import net.dblsaiko.illuminate.client.util.Program;
import net.dblsaiko.illuminate.client.util.ScalarUniform;

public final class ComposeShader extends Program {
    public final ScalarUniform mvp = this.uniform("mvp");
    public final ScalarUniform worldTex = this.uniform("worldTex");
    public final ScalarUniform lightTex = this.uniform("lightTex");

    public ComposeShader(int id) {
        super(id);
    }
}
