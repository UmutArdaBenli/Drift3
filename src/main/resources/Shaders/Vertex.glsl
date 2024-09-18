#version 330 core

layout(location = 0) in vec3 position;   // Vertex position
uniform mat4 mvpMatrix;                  // The combined Model-View-Projection matrix

out vec2 fragCoord;                      // Pass the fragment's screen coordinates to the fragment shader
out vec4 worldPosition;                  // The position of the vertex in world space

void main() {
    // Apply the MVP matrix for model rendering
    gl_Position = mvpMatrix * vec4(position, 1.0);

    // Store the world position (before projection) for the fragment shader
    worldPosition = vec4(position, 1.0);

    // Map vertex coordinates (-1 to 1) to (0 to 1) for screen coordinates (for the gradient sky rendering)
    fragCoord = gl_Position.xy * 0.5 + 0.5;
}
