#version 400 core

uniform mat4 mvp;

in vec4 vert;

out vec2 _xy;

void main() {
    // Dividing by w shouldn't be necessary here since this is a post processing shader, but do it anyway just in case
    _xy = vert.xy / vert.w;
    gl_Position = mvp * vert;
}