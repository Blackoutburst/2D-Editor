#version 410 core

in float layer;
in vec2 uv;
in vec4 color;

out vec4 FragColor;

uniform sampler2DArray diffuseMap;
uniform sampler2D missingDiffuseMap;


void main() {
    if (layer == -1) {
        FragColor = vec4(color) * texture(missingDiffuseMap, vec2(uv));
    } else {
        FragColor = vec4(color) * texture(diffuseMap, vec3(uv, layer));
    }


}