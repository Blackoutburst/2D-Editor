#version 410 core

layout(location = 0) in float aTexId;
layout(location = 1) in vec2 aPos;
layout(location = 2) in vec4 aColor;

out float textId;
out vec2 fragPos;
out vec4 color;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main() {
    textId = aTexId;
    color = aColor;
    gl_Position = projection * view * model * vec4(aPos, 0.0, 1.0);
}