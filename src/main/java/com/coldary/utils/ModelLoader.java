package com.coldary.utils;

import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL30.*;

public class ModelLoader {

    private int vaoID;
    private int vertexCount;
    private List<Integer> vbos = new ArrayList<>();
    private Map<String, Material> materials = new HashMap<>();

    public ModelLoader(InputStream objFilePath) {
        loadOBJ(objFilePath);
    }

    private void loadMTL(String mtlFileName, URL baseURL) {
        System.out.println("Loading MTL file from: " + mtlFileName); // Debugging

        Material currentMaterial = null;

        try (InputStream mtlFilePath = new URL(baseURL, mtlFileName).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(mtlFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Print the line being processed
                System.out.println("Processing line: " + line);

                String[] tokens = line.split("\\s+");
                if (line.startsWith("newmtl")) {
                    currentMaterial = new Material(tokens[1]);
                    materials.put(tokens[1], currentMaterial);
                } else if (line.startsWith("Kd") && currentMaterial != null) {
                    currentMaterial.setDiffuse(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
                } else if (line.startsWith("Ka") && currentMaterial != null) {
                    currentMaterial.setAmbient(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
                } else if (line.startsWith("Ks") && currentMaterial != null) {
                    currentMaterial.setSpecular(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number in line: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error processing line, not enough tokens: " + e.getMessage());
        }
    }

    private void loadOBJ(InputStream filePath) {
        List<float[]> vertices = new ArrayList<>();
        List<float[]> textures = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<int[]> indices = new ArrayList<>();

        System.out.println("Loading OBJ file from: " + filePath); // Debugging

        // Obtain the base URL for resolving mtl file paths
        URL baseURL = this.getClass().getResource("");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Print the line being processed
                System.out.println("Processing line: " + line);

                String[] tokens = line.split("\\s+");
                if (line.startsWith("mtllib")) {
                    // Load material file
                    loadMTL(tokens[1], baseURL);
                } else if (line.startsWith("v ")) {
                    // Vertex position
                    float[] vertex = {
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    };
                    vertices.add(vertex);
                } else if (line.startsWith("vt ")) {
                    // Texture coordinates
                    float[] texture = {
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    };
                    textures.add(texture);
                } else if (line.startsWith("vn ")) {
                    // Vertex normal
                    float[] normal = {
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    };
                    normals.add(normal);
                } else if (line.startsWith("f ")) {
                    // Face (triangle)
                    processFace(tokens, indices);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number in line: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error processing line, not enough tokens: " + e.getMessage());
        }

        // Load into OpenGL
        float[] verticesArray = listToArray(vertices, 3);
        float[] texturesArray = listToArray(textures, 2);
        float[] normalsArray = listToArray(normals, 3);
        int[] indicesArray = indicesToArray(indices);

        storeInVAO(verticesArray, texturesArray, normalsArray, indicesArray);
    }

    private void processFace(String[] tokens, List<int[]> indices) {
        for (int i = 1; i < tokens.length; i++) {
            String[] vertexData = tokens[i].split("/");
            int vertexIndex = Integer.parseInt(vertexData[0]) - 1;
            int textureIndex = vertexData.length > 1 && !vertexData[1].isEmpty() ? Integer.parseInt(vertexData[1]) - 1 : 0;
            int normalIndex = vertexData.length > 2 ? Integer.parseInt(vertexData[2]) - 1 : 0;

            indices.add(new int[]{vertexIndex, textureIndex, normalIndex});
        }
    }

    private float[] listToArray(List<float[]> list, int elementSize) {
        float[] array = new float[list.size() * elementSize];
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < elementSize; j++) {
                array[i * elementSize + j] = list.get(i)[j];
            }
        }
        return array;
    }

    private int[] indicesToArray(List<int[]> indices) {
        List<Integer> finalIndices = new ArrayList<>();
        for (int[] face : indices) {
            finalIndices.add(face[0]);
        }
        int[] array = new int[finalIndices.size()];
        for (int i = 0; i < finalIndices.size(); i++) {
            array[i] = finalIndices.get(i);
        }
        return array;
    }

    private void storeInVAO(float[] vertices, float[] textures, float[] normals, int[] indices) {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Store vertices
        storeDataInAttributeList(0, 3, vertices);

        // Store texture coordinates
        if (textures.length > 0) {
            storeDataInAttributeList(1, 2, textures);
        }

        // Store normals
        if (normals.length > 0) {
            storeDataInAttributeList(2, 3, normals);
        }

        // Bind indices
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        int indicesVBO = glGenBuffers();
        vbos.add(indicesVBO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);

        vertexCount = indices.length;

        // Unbind VAO
        glBindVertexArray(0);
    }

    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = glGenBuffers();
        vbos.add(vboID);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);

        MemoryUtil.memFree(buffer);
    }

    // Render the model with materials
    public void render() {
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        Material material = materials.values().stream().findFirst().orElse(Material.DEFAULT_MATERIAL);

        if (material != null) {
            // Set material properties (this would typically go to a shader)
        }

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    // Clean up VBOs and VAO
    public void cleanup() {
        glBindVertexArray(0);
        for (int vbo : vbos) {
            glDeleteBuffers(vbo);
        }
        glDeleteVertexArrays(vaoID);
    }

    static class Material {
        private String name;
        private float[] ambient = new float[]{0.2f, 0.2f, 0.2f};
        private float[] diffuse = new float[]{0.8f, 0.8f, 0.8f};
        private float[] specular = new float[]{1.0f, 1.0f, 1.0f};

        public static final Material DEFAULT_MATERIAL = new Material("default");

        public Material(String name) {
            this.name = name;
        }

        public void setAmbient(float r, float g, float b) {
            this.ambient = new float[]{r, g, b};
        }

        public void setDiffuse(float r, float g, float b) {
            this.diffuse = new float[]{r, g, b};
        }

        public void setSpecular(float r, float g, float b) {
            this.specular = new float[]{r, g, b};
        }

        @Override
        public String toString() {
            return "Material{" +
                    "name='" + name + '\'' +
                    ", ambient=" + ambient[0] + ", " + ambient[1] + ", " + ambient[2] +
                    ", diffuse=" + diffuse[0] + ", " + diffuse[1] + ", " + diffuse[2] +
                    ", specular=" + specular[0] + ", " + specular[1] + ", " + specular[2] +
                    '}';
        }
    }
}