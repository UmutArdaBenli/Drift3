package com.coldary;

import com.coldary.utils.InputHandler;
import com.coldary.utils.ModelLoader;
import com.coldary.utils.ResourceLoader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;

public class Main {

    // Window dimensions
    private int width = 1280;
    private int height = 720;
    private String title = "LWJGL 3D Model Rendering";

    // The window handle
    private long window;

    // Shader program
    private int shaderProgram;

    // Model loader
    private ModelLoader model;

    // Matrices
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f modelMatrix;
    Vector3f cameraPos;  // Camera positioned 5 units away from the origin
    Vector3f front;     // Looking at the origin

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        System.out.println("Starting LWJGL " + Version.getVersion() + "!");

        try (GLFWErrorCallback errorCallback = glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))) {
            init();
            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            glfwTerminate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Center the window
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Initialize OpenGL bindings
        GL.createCapabilities();

        // Initialize shaders and geometry
        shaderProgram = createShaderProgram();

        // Load the 3D model
        model = new ModelLoader(Main.class.getResourceAsStream("/bmw_m4.obj"));

        // Set up projection matrix (Perspective Projection)
        float fov = (float) Math.toRadians(60.0f);  // Field of view
        float aspectRatio = (float) width / height;  // Aspect ratio
        float near = 0.1f;
        float far = 100.0f;

        projectionMatrix = new Matrix4f().perspective(fov, aspectRatio, near, far);

        // Set up view matrix (Camera setup)
        cameraPos = new Vector3f(0.0f, 0.0f, 5.0f);
        front = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);         // 'Up' direction

        viewMatrix = new Matrix4f().lookAt(cameraPos, front, up);

        // Set up model matrix (identity, meaning no transformation)
        modelMatrix = new Matrix4f().identity();
    }

    private void loop() {
        // Set the clear color
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Render loop
        while (!glfwWindowShouldClose(window)) {
            // Clear the framebuffer and depth buffer
            GL11.glEnable(GL11.GL_DEPTH_TEST);  // Enable depth testing
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Use the shader program
            glUseProgram(shaderProgram);

            if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS){
                cameraPos.add(front.mul(1));
                System.out.println("vector");
            }

            // Create the MVP (Model-View-Projection) matrix
            Matrix4f mvpMatrix = new Matrix4f();
            projectionMatrix.mul(viewMatrix, mvpMatrix);  // projection * view
            mvpMatrix.mul(modelMatrix);                   // (projection * view) * model

            // Pass the MVP matrix to the shader
            int mvpLocation = glGetUniformLocation(shaderProgram, "mvpMatrix");
            float[] mvpArray = new float[16];
            mvpMatrix.get(mvpArray);
            glUniformMatrix4fv(mvpLocation, false, mvpArray);

            // Render the 3D model
            model.render();

            // Swap the color buffers
            glfwSwapBuffers(window);

            // Poll for window events
            glfwPollEvents();
        }
        model.cleanup();
    }

    private int createShaderProgram() {
        // Load and compile vertex shader
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, ResourceLoader.readFileFromResources("Shaders/Vertex.glsl"));
        glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        // Load and compile fragment shader
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, ResourceLoader.readFileFromResources("Shaders/Fragment.glsl"));
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
}
