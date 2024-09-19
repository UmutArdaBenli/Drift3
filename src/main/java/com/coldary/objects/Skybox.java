package com.coldary.objects;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import org.joml.Matrix4f;
import com.coldary.utils.Shaders;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.List;

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
        GL2 gl = GLContext.getCurrentGL().getGL2();

        shaderProgram = new Shaders("/Shaders/skybox/Vertex.skybox.glsl", "/Shaders/skybox/Fragment.skybox.glsl");

        int[] vao = new int[1];
        int[] vbo = new int[1];
        gl.glGenVertexArrays(1, vao, 0);
        gl.glGenBuffers(1, vbo, 0);
        vaoID = vao[0];
        vboID = vbo[0];

        gl.glBindVertexArray(vaoID);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboID);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, Buffers.newDirectFloatBuffer(vertices), GL2.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 3 * Float.BYTES, 0);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        gl.glBindVertexArray(0);

        textureID = loadCubeMap(gl, faces);
    }

    private int loadCubeMap(GL2 gl, List<String> faces) {
        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        textureID = textures[0];
        gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, textureID);

        for (int i = 0; i < faces.size(); i++) {
            String facePath = faces.get(i);
            try (InputStream is = getClass().getResourceAsStream(facePath)) {
                if (is == null) {
                    System.err.println("Failed to find texture file: " + facePath);
                    continue;
                }

                Texture texture = TextureIO.newTexture(is, true, TextureIO.JPG);
                texture.bind(gl);
                gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL2.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, texture.getPixels());
            } catch (IOException e) {
                System.err.println("Failed to load texture file: " + facePath);
                e.printStackTrace();
            }
        }

        gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_EDGE);

        gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, 0);
        return textureID;
    }

    public void render(GL2 gl, Matrix4f view, Matrix4f projection) {
        shaderProgram.start();

        FloatBuffer viewBuffer = Buffers.newDirectFloatBuffer(16);
        view.get(viewBuffer);
        shaderProgram.loadMatrix(shaderProgram.getUniformLocation("view"), viewBuffer);

        FloatBuffer projectionBuffer = Buffers.newDirectFloatBuffer(16);
        projection.get(projectionBuffer);
        shaderProgram.loadMatrix(shaderProgram.getUniformLocation("projection"), projectionBuffer);

        gl.glBindVertexArray(vaoID);
        gl.glActiveTexture(GL2.GL_TEXTURE0);
        gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, textureID);
        gl.glDrawArrays(GL2.GL_TRIANGLES, 0, 36);
        gl.glBindVertexArray(0);

        shaderProgram.stop();
    }

    public void cleanup(GL2 gl) {
        gl.glDeleteVertexArrays(1, new int[]{vaoID}, 0);
        gl.glDeleteBuffers(1, new int[]{vboID}, 0);
        gl.glDeleteTextures(1, new int[]{textureID}, 0);
        shaderProgram.cleanUp();
    }
}