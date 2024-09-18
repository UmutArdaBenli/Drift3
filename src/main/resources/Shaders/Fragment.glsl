#version 330 core

out vec4 fragColor;

uniform vec3 topColor;    // The color at the top of the sky
uniform vec3 bottomColor; // The color at the bottom of the sky

in vec2 fragCoord;        // The fragment's screen coordinates from the vertex shader
in vec4 worldPosition;    // World position for model rendering

uniform bool isSkyRendering; // A flag to toggle between sky and model rendering

void main() {
    if (isSkyRendering) {
        // If rendering the sky, create the gradient
        float mixFactor = fragCoord.y;
        vec3 skyColor = mix(topColor, bottomColor, mixFactor);
        fragColor = vec4(skyColor, 1.0);  // Output the gradient color for the sky
    } else {
        // If rendering the model, use a fixed color (modify as needed)
        fragColor = vec4(1.0, 0.5, 0.2, 1.0);  // Simple color for the model
    }
}
