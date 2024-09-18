#version 330 core

in vec3 fragNormal;
in vec3 fragPosition;

out vec4 color;

uniform vec3 ambient;
uniform vec3 diffuse;
uniform vec3 specular;
uniform vec3 lightColor;
uniform vec3 lightPos;
uniform vec3 viewPos;
uniform float shininess;

void main() {
    // Ambient
    vec3 ambientComponent = ambient * lightColor;

    // Diffuse
    vec3 norm = normalize(fragNormal);
    vec3 lightDir = normalize(lightPos - fragPosition);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuseComponent = diff * diffuse * lightColor;

    // Specular
    vec3 viewDir = normalize(viewPos - fragPosition);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
    vec3 specularComponent = spec * specular * lightColor;

    vec3 result = ambientComponent + diffuseComponent + specularComponent;
    color = vec4(result, 1.0);
}