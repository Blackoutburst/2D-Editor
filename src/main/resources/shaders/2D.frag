#version 410 core

uniform sampler2D diffuseMap;
uniform float alpha;

in vec2 uv;
out vec4 FragColor;

void main() {
    FragColor = vec4(1.0, 1.0, 1.0, alpha) * texture(diffuseMap, uv);
}