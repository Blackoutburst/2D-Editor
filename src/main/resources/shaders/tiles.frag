#version 410 core

in float textId;
in vec4 color;

out vec4 FragColor;

void main() {
    FragColor = vec4(color);
}