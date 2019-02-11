#version 130
#extension GL_ARB_gpu_shader5 : enable

uniform mat4 mvp;

uniform int width;
uniform int height;

out vec2 _uv;
out vec2 _xy;

flat out mat4 camInv;

void main() {
    _xy = gl_Vertex.xy / gl_Vertex.w;
    _uv = _xy / vec2(width, height);
    camInv = inverse(gl_ModelViewProjectionMatrix);
    gl_Position = mvp * gl_Vertex;
}