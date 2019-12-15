#version 400 core

uniform mat4 mvp;
uniform mat4 camInv;

uniform int width;
uniform int height;

in vec4 vert;

out vec2 _uv;
out vec2 _xy;

void main() {
    _xy = vert.xy / vert.w;
    _uv = _xy / vec2(width, height);
    gl_Position = mvp * vert;
}