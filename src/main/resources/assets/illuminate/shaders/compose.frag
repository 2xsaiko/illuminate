#version 400 core

uniform sampler2D worldTex;
uniform sampler2D lightTex;

in vec2 f_uv;

out vec4 color;

void main() {
    vec4 world = texture(worldTex, f_uv);
    vec4 light = texture(lightTex, f_uv);

    color = world + world * light;
}
