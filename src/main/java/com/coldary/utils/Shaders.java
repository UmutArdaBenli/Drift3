package com.coldary.utils;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        checkCompileErrors(shaderProgram, "PROGRAM");
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
            throw new RuntimeException("Failed to load shader: " + filePath, e);
        }

        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        checkCompileErrors(shaderID, getShaderTypeString(type));
        return shaderID;
    }

    private String getShaderTypeString(int type) {
        switch (type) {
            case GL_VERTEX_SHADER: return "VERTEX";
            case GL_FRAGMENT_SHADER: return "FRAGMENT";
            default: return "UNKNOWN";
        }
    }

    private void checkCompileErrors(int shader, String type) {
        int success = (type.equals("PROGRAM")) ? glGetProgrami(shader, GL_LINK_STATUS) : glGetShaderi(shader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String log = (type.equals("PROGRAM")) ? glGetProgramInfoLog(shader) : glGetShaderInfoLog(shader);
            throw new RuntimeException(type + " compilation error: " + log);
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