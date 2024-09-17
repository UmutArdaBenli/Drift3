package com.coldary.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;

public class Shaders {

    public static int createShaderProgram() {
        // Load and compile vertex shader
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, readFileAsString("vertex.glsl"));
        glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        // Load and compile fragment shader
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, readFileAsString("fragment.glsl"));
        glCompileShader(fragmentShader);
        checkCompileErrors(fragmentShader, "FRAGMENT");

        // Link shaders to program
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        checkCompileErrors(shaderProgram, "PROGRAM");

        // Clean up shaders (no longer needed after linking)
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return shaderProgram;
    }

    private static String readFileAsString(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    private static void checkCompileErrors(int shader, String type) {
        int success;
        if (type.equals("PROGRAM")) {
            success = glGetProgrami(shader, GL_LINK_STATUS);
            if (success == GL_FALSE) {
                System.err.println("Error: Program linking failed.");
                System.err.println(glGetProgramInfoLog(shader));
            }
        } else {
            success = glGetShaderi(shader, GL_COMPILE_STATUS);
            if (success == GL_FALSE) {
                System.err.println("Error: " + type + " shader compilation failed.");
                System.err.println(glGetShaderInfoLog(shader));
            }
        }
    }
}
