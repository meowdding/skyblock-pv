#version 150
precision highp int;

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform mat4x4 colors;
uniform int states;
uniform int ticks;

in float vertexDistance;
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

int colorAt(int index) {
    return int(colors[index / 4][index % 4]);
}

vec4 RAMP(float x) {
    x *= (states - 1);
    return mix(fromARGB(colorAt(int(x))), fromARGB(colorAt(int(x) + 1)), smoothstep(0.0, 1.0, fract(x)));
}

float clampZeroOne(float value) {
    return min(max(value, 0), 1);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    vec2 coords = gl_FragCoord.xy;

    fragColor = vec4(RAMP(float(int(coords.x + ticks * 2) % 500) / 500.0).rgb, 1) * vec4(vertexColor.rgb, 1);
}
