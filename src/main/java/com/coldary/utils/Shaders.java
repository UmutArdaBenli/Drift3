package com.coldary.utils;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.*;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Shaders {


    private int shaderProgram;
    private int vertexShaderID;
    private int fragmentShaderID;

    public Shaders(String vertexFile, String fragmentFile) {
        vertexShaderID = loadShader(vertexFile, GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL_FRAGMENT_SHADER);
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShaderID);
        glAttachShader(shaderProgram, fragmentShaderID);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Program Linking: " + glGetProgramInfoLog(shaderProgram));
            System.exit(1);
        }
        glValidateProgram(shaderProgram);
    }
    private int loadShader(String filePath, int type) {
        StringBuilder shaderSource = new StringBuilder();

        try (InputStream inputStream = getClass().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Shader Compilation: " + glGetShaderInfoLog(shaderID));
            System.exit(1);
        }
        return shaderID;
    }
    public int createShaderProgram() {
        // Load and compile vertex shader
        vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderID, ResourceLoader.readFileFromResources("Shaders/Vertex.glsl"));
        glCompileShader(vertexShaderID);
        checkCompileErrors(vertexShaderID, "VERTEX");

        // Load and compile fragment shader
        fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderID, ResourceLoader.readFileFromResources("Shaders/Fragment.glsl"));
        glCompileShader(fragmentShaderID);
        checkCompileErrors(fragmentShaderID, "FRAGMENT");

        // Link shaders to program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShaderID);
        glAttachShader(shaderProgram, fragmentShaderID);
        glLinkProgram(shaderProgram);
        checkCompileErrors(shaderProgram, "PROGRAM");

        // Clean up shaders (no longer needed after linking)
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);

        return shaderProgram;
    }

    private void checkCompileErrors(int shader, String type) {
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

    public void setMatrixUniform(int program, String name, Matrix4f matrix) {
        int location = glGetUniformLocation(program, name);
        try (MemoryStack stack = stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    public void loadMatrix(int location, FloatBuffer matrix) {
        glUniformMatrix4fv(location, false, matrix);
    }
    public void start() {
        glUseProgram(shaderProgram);
    }
    public void stop() {
        glUseProgram(0);
    }

    public void cleanUp() {
        stop();
        glDetachShader(shaderProgram, vertexShaderID);
        glDetachShader(shaderProgram, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteProgram(shaderProgram);
    }
    public int getUniformLocation(String uniformName) {
        return glGetUniformLocation(shaderProgram, uniformName);
    }
    public int getShaderProgram() {
        return shaderProgram;
    }

    public int getVertexShaderID() {
        return vertexShaderID;
    }

    public int getFragmentShaderID() {
        return fragmentShaderID;
    }
}
