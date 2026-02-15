#version 410 core

layout(location = 0) in float aLayer;
layout(location = 1) in vec2 aPos;
layout(location = 2) in vec2 aUv;
layout(location = 3) in vec4 aColor;

out float layer;
out vec2 fragPos;
out vec2 uv;
out vec4 color;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main() {
    layer = aLayer;
    color = aColor;
    uv = aUv;
    gl_Position = projection * view * model * vec4(aPos, 0.0, 1.0);
}