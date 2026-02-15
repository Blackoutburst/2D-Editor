#version 410 core

in float layer;
in vec2 uv;
in vec4 color;

out vec4 FragColor;

uniform sampler2DArray diffuseMap;


void main() {
    FragColor = vec4(color) * texture(diffuseMap, vec3(uv, layer));
}