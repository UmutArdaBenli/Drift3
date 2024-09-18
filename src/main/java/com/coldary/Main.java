package com.coldary;

import com.coldary.objects.Camera;
import com.coldary.utils.InputHandler;
import com.coldary.utils.ModelLoader;
import com.coldary.utils.ResourceLoader;
import com.coldary.utils.Shaders;
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
    public int width = 1280;
    public int height = 720;
    private String title = "LWJGL 3D Model Rendering";

    // The window handle
    private long window;

    // Shader program
    public int shaderProgram;

    // Camera object
    private Camera camera;

    // Model loader
    private ModelLoader model;

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

            // terminate at the end of the loop
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
        shaderProgram = Shaders.createShaderProgram();
        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                camera.processMouseMovement((float) xpos, (float) ypos);
            }
        });
        // Load the 3D model
        model = new ModelLoader(Main.class.getResourceAsStream("/bmw_m4.obj"));

        camera = new Camera(new Vector3f(0,0,0),new Vector3f(0,1,0),-90.0f,0);
    }

    private void loop() {
        // Set the clear color
        GL11.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);

        // Render loop
        while (!glfwWindowShouldClose(window)) {
            // Clear the framebuffer and depth buffer
            GL11.glEnable(GL11.GL_DEPTH_TEST);  // Enable depth testing
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            //renderGradientBackground();
            // Use the shader program
            glUseProgram(shaderProgram);
            System.out.println(camera.getYaw() + " " + camera.getPitch());
            // Render the 3D model
            model.render();

            // Swap the color buffers
            glfwSwapBuffers(window);

            // Poll for window events
            glfwPollEvents();
        }
        model.cleanup();
    }

    private void renderGradientBackground() {
        // Set the gradient colors (you can modify these as you like)
        // Render the sky (before rendering the 3D model)
        glUseProgram(shaderProgram);
        glUniform1i(glGetUniformLocation(shaderProgram, "isSkyRendering"), 1);  // Set sky rendering to true
        glUniform3f(glGetUniformLocation(shaderProgram, "topColor"), 0.1f, 0.4f, 1.0f);   // Top sky color (light blue)
        glUniform3f(glGetUniformLocation(shaderProgram, "bottomColor"), 0.8f, 0.9f, 1.0f); // Bottom sky color (pale blue)
    // Render a full-screen quad or use another approach to render the background sky

    // Render the 3D model (after rendering the sky)
        glUniform1i(glGetUniformLocation(shaderProgram, "isSkyRendering"), 0);  // Set sky rendering to false
    // Bind and render the model here


        // Render a full-screen quad
        glBegin(GL_QUADS);
            glVertex2f(-1.0f, -1.0f);
            glVertex2f( 1.0f, -1.0f);
            glVertex2f( 1.0f,  1.0f);
            glVertex2f(-1.0f,  1.0f);
        glEnd();
    }
}
