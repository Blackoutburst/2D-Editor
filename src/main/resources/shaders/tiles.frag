#version 410 core

in float layer;
in vec2 uv;
in vec4 color;

out vec4 FragColor;

uniform sampler2DArray diffuseMap;
uniform sampler2D missingDiffuseMap;

uniform float showLayer;
uniform vec4 layerColor;

#define OUTLINE 0.02

void main() {
    if (layer == -1) {
        FragColor = vec4(color) * texture(missingDiffuseMap, vec2(uv));
    } else {
        FragColor = vec4(color) * texture(diffuseMap, vec3(uv, layer));
    }

    if (showLayer == 1.0 && (uv.x < OUTLINE || uv.x > 1.0 - OUTLINE || uv.y < OUTLINE || uv.y > 1.0 - OUTLINE)) {
        FragColor = layerColor;
    }
}