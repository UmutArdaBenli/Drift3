package com.coldary;

import com.coldary.objects.Camera;
import com.coldary.utils.ModelLoader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class MainApplication {

    private long window;
    private ModelLoader model;
    private Camera camera;

    public static void main(String[] args) {
        new MainApplication().run();
    }

    public void run() {
        init();
        loop();
        glfwTerminate();
    }

    private void init() {
        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        
        // Create window
        window = glfwCreateWindow(800, 600, "LWJGL Model Loader", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // V-sync
        glfwShowWindow(window);

        // Initialize OpenGL bindings
        GL.createCapabilities();

        // Load model
        InputStream modelStream = getClass().getResourceAsStream("/bmw_m4.obj");
        model = new ModelLoader(modelStream);

        // Compile and link shaders
        int vertexShader = compileShader(GL_VERTEX_SHADER, "#version 330 core\n" +
            "layout(location = 0) in vec3 aPos;\n" +
            "layout(location = 1) in vec2 aTexCoord;\n" +
            "layout(location = 2) in vec3 aNormal;\n" +
            "uniform mat4 model;\n" +
            "uniform mat4 view;\n" +
            "uniform mat4 projection;\n" +
            "void main() {\n" +
            "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
            "}\n");
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, "#version 330 core\n" +
            "out vec4 FragColor;\n" +
            "void main() {\n" +
            "    FragColor = vec4(1.0, 0.5, 0.2, 1.0);\n" +
            "}\n");
        int shaderProgram = linkShaders(vertexShader, fragmentShader);
        glUseProgram(shaderProgram);
        camera = new Camera(new Vector3f(0,0,0),new Vector3f(0,1,0),-90.0f,0);
        /*
        // Set up transformation matrices
        try (MemoryStack stack = stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            new Matrix4f().identity().get(fb);
            int modelLoc = glGetUniformLocation(shaderProgram, "model");
            glUniformMatrix4fv(modelLoc, false, fb);
            new Matrix4f().lookAt(new Vector3f(0, 0, 3), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0)).get(fb);
            int viewLoc = glGetUniformLocation(shaderProgram, "view");
            glUniformMatrix4fv(viewLoc, false, fb);
            new Matrix4f().perspective((float) Math.toRadians(45.0f), 800.0f / 600.0f, 0.1f, 100.0f).get(fb);
            int projectionLoc = glGetUniformLocation(shaderProgram, "projection");
            glUniformMatrix4fv(projectionLoc, false, fb);
        }
         */
    }

    private void loop() {
        glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
        while (!glfwWindowShouldClose(window)) {
            // Clear the framebuffer and depth buffer
            glEnable(GL_DEPTH_TEST);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Render the model
            model.render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        model.cleanup();
    }

    private int compileShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Failed to compile shader: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private int linkShaders(int vertexShader, int fragmentShader) {
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Failed to link shaders: " + glGetProgramInfoLog(program));
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return program;
    }
}