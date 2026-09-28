#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

#ifdef NO_LAYOUT

out vec2 texCoord0;
out vec4 vertexColor;

in vec3 Position;
in vec2 UV0;
in vec4 Color;
#else
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) out vec2 texCoord0;
layout(location = 1) out vec4 vertexColor;

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;
#endif

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
}
