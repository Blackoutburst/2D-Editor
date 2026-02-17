#version 410 core

layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aUv;

out vec2 uv;
out vec2 fragPos;

uniform mat4 projection;
uniform mat4 model;
uniform mat4 view;

void main() {
    uv = aUv;
    gl_Position = projection * view * model * vec4(aPos, 0.0, 1.0);
}