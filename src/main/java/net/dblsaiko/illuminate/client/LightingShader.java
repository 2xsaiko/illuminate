package net.dblsaiko.illuminate.client;

import net.dblsaiko.illuminate.client.util.Program;
import net.dblsaiko.illuminate.client.util.ScalarUniform;
import net.dblsaiko.illuminate.client.util.UniformArray;

public final class LightingShader extends Program {
    public final ScalarUniform mvp = this.uniform("mvp");
    public final ScalarUniform width = this.uniform("width");
    public final ScalarUniform height = this.uniform("height");
    public final ScalarUniform accum = this.uniform("accum");
    public final ScalarUniform depth = this.uniform("depth");
    private final int maxLights = IlluminateClient.instance().getMaxLights();
    public final UniformArray texTable = this.uniformArray("texTable", this.maxLights);
    public final UniformArray lightTex = this.uniformArray("lightTex", this.maxLights);
    public final UniformArray lightDepth = this.uniformArray("lightDepth", this.maxLights);
    public final UniformArray lightCam = this.uniformArray("lightCam", this.maxLights);
    public final UniformArray lightPos = this.uniformArray("lightPos", this.maxLights);
    public final UniformArray lightBrightness = this.uniformArray("lightBrightness", this.maxLights);
    public final ScalarUniform lightCount = this.uniform("lightCount");
    public final ScalarUniform camInv = this.uniform("camInv");

    public LightingShader(int id) {
        super(id);
    }
}
