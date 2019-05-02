#version 120

#define NUM_LIGHTS 10

uniform int width;
uniform int height;

uniform sampler2D world;
uniform sampler2D depth;

uniform sampler2D[NUM_LIGHTS] lightTex;
uniform sampler2D[NUM_LIGHTS] lightDepth;
uniform mat4[NUM_LIGHTS] lightCam;
uniform vec3[NUM_LIGHTS] lightPos;
uniform int lightCount;

uniform mat4 screen2tex = mat4(
    0.5, 0,   0,   0,
    0,   0.5, 0,   0,
    0,   0,   0.5, 0,
    0.5, 0.5, 0.5, 1
);

uniform mat4 camInv;

varying vec2 _uv;
varying vec2 _xy;

bool isInBox(in vec3 v) {
    return
        v.x >= -1 && v.x <= 1 &&
        v.y >= -1 && v.y <= 1 &&
        v.z >= -1 && v.z <= 1;
}

vec3 toWorldCoords(in vec2 screen, in float depth) {
    vec4 c = vec4(screen.xy / vec2(width, height) * 2 - vec2(1, 1), depth, 1);
    vec4 r = camInv * c;

    return r.xyz / r.w;
}

vec3 toWorldCoords(in vec2 screen, in vec2 depthCoords) {
    float d = texture2D(depth, depthCoords).x * 2 - 1;
    return toWorldCoords(screen, d);
}

vec3 getNormal(in vec2 screen, in vec2 depthCoords) {
    vec2 scd = vec2(width, height);
    vec3 a = toWorldCoords(screen + vec2( 1,  0), depthCoords + (vec2( 1,  0) / scd));
    vec3 b = toWorldCoords(screen + vec2(-1, -1), depthCoords + (vec2(-1, -1) / scd));
    vec3 c = toWorldCoords(screen + vec2(-1,  1), depthCoords + (vec2(-1,  1) / scd));

    return -normalize(cross(b - a, c - a));
}

void main() {
    float depthR = texture2D(depth, _uv).x;
    float depth = depthR * 2 - 1;
    vec3 worldCoords = toWorldCoords(_xy, depth);

    vec3 rgb = texture2D(world, _uv).xyz;

    vec3 combinedLightColor = vec3(0);

    vec3 normal = getNormal(_xy, _uv);

    for (int i = 0; i < lightCount; i++) {
        vec3 dir = lightPos[i] - worldCoords;
        float lmul = clamp(dot(normalize(dir), normal), 0, 1);
        float dist = length(dir);
        vec4 v = lightCam[i] * vec4(worldCoords, 1);
        vec4 tex = screen2tex * v;
        vec3 lightCamCoords = v.xyz / v.w;
        vec2 texCoords = tex.xy / tex.w;
        float ld = texture2D(lightDepth[i], texCoords).x * 2 - 1;
        if (isInBox(lightCamCoords) && lightCamCoords.z <= ld + 0.001) {
            vec4 texColor = texture2D(lightTex[i], texCoords * vec2(1, -1));
            combinedLightColor += vec3(texColor.xyz * texColor.w * lmul * inversesqrt(dist));
        }
    }

    gl_FragColor = vec4(rgb + combinedLightColor.xyz, 1);
}