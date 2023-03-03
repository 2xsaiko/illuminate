#version 400 core

uniform mat4 mvp;
uniform mat4 camInv;

uniform int width;
uniform int height;

in vec4 vert;

out vec2 _uv;
out vec2 _xy;

void main() {
    // Dividing by w shouldn't be necessary here since this is a post processing shader, but do it anyway just in case
    _xy = vert.xy / vert.w;
    // These coordinates are in pixels, so divide by window width/height
    _uv = _xy / vec2(width, height);
    gl_Position = mvp * vert;
}