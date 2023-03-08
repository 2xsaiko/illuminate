#version 400 core

uniform mat4 mvp;

in vec4 vert;
in vec2 uv;

out vec2 f_uv;

void main() {
    f_uv = uv;
    gl_Position = mvp * vert;
}