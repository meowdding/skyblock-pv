#version 150
precision highp int;

//!moj_import <minecraft:dynamictransforms.glsl>
//!moj_import <minecraft:projection.glsl>
//!moj_import <minecraft:globals.glsl>
//!moj_import <minecraft:fog.glsl>

uniform sampler2D Sampler0;

const int colors[] = COLORS;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float from8Bit(int color) {
    return float(color) / 255.0;
}

vec4 fromARGB(int color) {
    float r = from8Bit((color & 0xFF0000) >> 16);
    float g = from8Bit((color & 0xFF00) >> 8);
    float b = from8Bit(color & 0xFF);
    return vec4(r, g, b, 1);
}

vec4 SMOOTHY(float x) {
    x *= (colors.length() - 1);
    return mix(fromARGB(colors[int(x)]), fromARGB(colors[int(x) + 1]), smoothstep(0.0, 1.0, fract(x)));
}

float clampZeroOne(float value) {
    return min(max(value, 0), 1);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    vec4 finalColor = vertexColor;

    if (finalColor.r != 0.0 || finalColor.g != 0.0 || finalColor.b != 0.0) {
        vec2 coords = gl_FragCoord.xy;
        finalColor = vec4(SMOOTHY(float(int(coords.x + (GameTime * 24000) * 2) % 500) / 500.0).rgb, 1) * vec4(vertexColor.rgb, 1.0);
    }

    fragColor = apply_fog(
        finalColor,
        sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd,
        FogRenderDistanceStart, FogRenderDistanceEnd,
        FogColor
    );
}
