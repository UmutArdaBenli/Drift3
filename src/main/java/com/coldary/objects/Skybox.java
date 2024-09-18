package com.coldary.objects;

import org.joml.Matrix4f;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import com.coldary.utils.Shaders;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

public class Skybox {
    private final float[] vertices = {
            // positions
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f,  1.0f
    };

    private int vaoID;
    private int vboID;
    private int textureID;
    private Shaders shaderProgram;

    public Skybox(List<String> faces) {
        shaderProgram = new Shaders("/Shaders/skybox/skybox_vertex.glsl", "/Shaders/skybox/skybox_fragment.glsl");

        vaoID = glGenVertexArrays();
        vboID = glGenBuffers();

        glBindVertexArray(vaoID);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        textureID = loadCubeMap(faces);
    }

    private int loadCubeMap(List<String> faces) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            for (int i = 0; i < faces.size(); i++) {
                ByteBuffer image;
                try {
                    image = STBImage.stbi_load(faces.get(i), width, height, channels, 4);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load texture file: " + faces.get(i), e);
                }

                if (image == null) {
                    System.err.println("Failed to load texture: " + faces.get(i) + " " + STBImage.stbi_failure_reason());
                    STBImage.stbi_image_free(image);
                    continue; // or handle the error appropriately
                }

                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
                STBImage.stbi_image_free(image);
            }
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        return textureID;
    }

    public void render(Matrix4f view, Matrix4f projection) {
        shaderProgram.start();

        shaderProgram.loadMatrix(shaderProgram.getUniformLocation("view"), FloatBuffer.wrap(view.get(new float[16]))); // remove translation component
        shaderProgram.loadMatrix(shaderProgram.getUniformLocation("projection"), FloatBuffer.wrap(projection.get(new float[16])));

        glBindVertexArray(vaoID);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        shaderProgram.stop();
    }

    public void cleanup() {
        glDeleteVertexArrays(vaoID);
        glDeleteBuffers(vboID);
        glDeleteTextures(textureID);
        shaderProgram.cleanUp();
    }
}