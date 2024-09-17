#version 330 core

layout(location = 0) in vec3 position;  // Vertex position
uniform mat4 mvpMatrix;  // The combined Model-View-Projection matrix

void main() {
    gl_Position = mvpMatrix * vec4(position, 1.0);
}
