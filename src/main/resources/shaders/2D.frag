#version 410 core

uniform sampler2D diffuseMap;
uniform float alpha;
uniform float borderRadius;

in vec2 uv;
out vec4 FragColor;

float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 p = uv - vec2(0.5);
    vec2 halfSize = vec2(0.5);
    float r = min(borderRadius, min(halfSize.x, halfSize.y));
    float dist = sdRoundedBox(p, halfSize, r);

    if (dist > 0.0) {
        discard;
    }

    FragColor = vec4(1.0, 1.0, 1.0, alpha) * texture(diffuseMap, uv);
}