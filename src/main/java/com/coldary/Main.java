package com.coldary;

import com.coldary.objects.Camera;
import com.coldary.objects.Skybox;
import com.coldary.utils.ModelLoader;
import com.coldary.utils.Shaders;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
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
    private Shaders shader;
    private int shaderProgram;

    // Scene objects
    Skybox skybox;
    private Camera camera;

    //Camera utils
    private double lastX, lastY;
    private boolean firstMouse = true;

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

            // Terminate GLFW
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
        shader = new Shaders("/Shaders/skybox/Vertex.skybox.glsl", "/Shaders/skybox/Fragment.skybox.glsl");
        //shader.createShaderProgram();
        shaderProgram = shader.getShaderProgram();

        // Load the 3D model
        model = new ModelLoader(Main.class.getResourceAsStream("/Objects/Cube.obj"));

        // Initialize Camera
        camera = new Camera(new Vector3f(0, 0, 3), new Vector3f(0, 1, 0), -90.0f, 0);

        glfwSetCursorPosCallback(window, this::mouseCallback);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        List<String> faces = List.of(
                "/null_Plainsky/null_plainsky512_dn.jpg",
                "/null_Plainsky/null_plainsky512_dn.jpg",
                "/null_Plainsky/null_plainsky512_dn.jpg",
                "/null_Plainsky/null_plainsky512_dn.jpg",
                "/null_Plainsky/null_plainsky512_dn.jpg",
                "/null_Plainsky/null_plainsky512_dn.jpg"
        );
        skybox = new Skybox(faces);
    }

    private void loop() {
        // Set the clear color
        GL11.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);

        // Enable depth testing
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // Render loop
        while (!glfwWindowShouldClose(window)) {
            // Clear the framebuffer and depth buffer
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Set the view and projection matrices for the skybox
            Matrix4f view = new Matrix4f(camera.getViewMatrix()).m30(0).m31(0).m32(0); // Remove translation
            Matrix4f projection = camera.getPerspectiveMatrix();

            // Render the skybox first
            skybox.render(view, projection);

            // Use the shader program for the model
            glUseProgram(shaderProgram);

            // Set the view and projection matrices for the model shader
            shader.setMatrixUniform(shaderProgram, "view", camera.getViewMatrix());
            shader.setMatrixUniform(shaderProgram, "projection", camera.getPerspectiveMatrix());

            // Model matrix
            Matrix4f matrixModel = new Matrix4f().identity();

            int viewPosLoc = glGetUniformLocation(shaderProgram, "viewPos");
            glUniform3f(viewPosLoc, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);

            int lightPosLoc = glGetUniformLocation(shaderProgram, "light.position");
            glUniform3f(lightPosLoc, 1.2f, 1.0f, 2.0f);

            int lightAmbientLoc = glGetUniformLocation(shaderProgram, "light.ambient");
            glUniform3f(lightAmbientLoc, 0.2f, 0.2f, 0.2f);

            int lightDiffuseLoc = glGetUniformLocation(shaderProgram, "light.diffuse");
            glUniform3f(lightDiffuseLoc, 0.5f, 0.5f, 0.5f);

            int lightSpecularLoc = glGetUniformLocation(shaderProgram, "light.specular");
            glUniform3f(lightSpecularLoc, 1.0f, 1.0f, 1.0f);

            // Set the material properties
            int matAmbientLoc = glGetUniformLocation(shaderProgram, "material.ambient");
            glUniform3f(matAmbientLoc, 1.0f, 0.5f, 0.31f);

            int matDiffuseLoc = glGetUniformLocation(shaderProgram, "material.diffuse");
            glUniform3f(matDiffuseLoc, 1.0f, 0.5f, 0.31f);

            int matSpecularLoc = glGetUniformLocation(shaderProgram, "material.specular");
            glUniform3f(matSpecularLoc, 0.5f, 0.5f, 0.5f);

            int matShineLoc = glGetUniformLocation(shaderProgram, "material.shininess");
            glUniform1f(matShineLoc, 32.0f);

            shader.setMatrixUniform(shaderProgram, "model", matrixModel);  // Set the model matrix uniform

            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
                camera.processKeyboardInput(GLFW_KEY_W, 0.0005f);
            }
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                camera.processKeyboardInput(GLFW_KEY_S, 0.0005f);
            }
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
                camera.processKeyboardInput(GLFW_KEY_A, 0.0005f);
            }
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
                camera.processKeyboardInput(GLFW_KEY_D, 0.0005f);
            }
            if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
                camera.processKeyboardInput(GLFW_KEY_Q, 0.0005f);
            }
            if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
                camera.processKeyboardInput(GLFW_KEY_E, 0.0005f);
            }

            // Render the 3D model
            model.render();

            System.out.println(camera.getYaw() + " " + camera.getPitch());

            // Swap the color buffers
            glfwSwapBuffers(window);

            // Poll for window events
            glfwPollEvents();
        }

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        // Cleanup
        model.cleanup();
        shader.cleanUp();
        skybox.cleanup();
    }

    private void mouseCallback(long window, double xpos, double ypos) {
        if (firstMouse) {
            lastX = xpos;
            lastY = ypos;
            firstMouse = false;
        }

        float xOffset = (float) (xpos - lastX);
        float yOffset = (float) (lastY - ypos); // reversed since y-coordinates range from bottom to top
        lastX = xpos;
        lastY = ypos;

        camera.processMouseMovement(xOffset, yOffset);
    }
}