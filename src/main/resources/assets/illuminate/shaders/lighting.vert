#version 130

uniform mat4 mvp;
uniform mat4 camInv;

uniform int width;
uniform int height;

out vec2 _uv;
out vec2 _xy;

void main() {
    _xy = gl_Vertex.xy / gl_Vertex.w;
    _uv = _xy / vec2(width, height);
    gl_Position = mvp * gl_Vertex;
}