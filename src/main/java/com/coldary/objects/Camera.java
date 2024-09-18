package com.coldary.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f worldUp;


    private float yaw;
    private float pitch;

    private float movementSpeed;
    private float mouseSensitivity;
    private float zoom;

    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        this.position = position;
        this.up = up;
        this.yaw = yaw;
        this.pitch = pitch;
        this.front = new Vector3f(0.0f, 0.0f, -1.0f);
        this.worldUp = up;
        this.right = new Vector3f();
        this.movementSpeed = 2.5f;
        this.mouseSensitivity = 0.05f;
        this.zoom = 45.0f;

        // Default projection parameters
        this.fov = 70.0f;
        this.aspectRatio = 16.0f / 9.0f; // Assuming default aspect ratio
        this.nearPlane = 0.1f;
        this.farPlane = 100.0f;

        // Initialize matrices
        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();

        updateCameraVectors();
        updateViewMatrix();
        updateProjectionMatrix();
    }


    public void processKeyboardInput(int key, float deltaTime) {
        float velocity = movementSpeed * deltaTime;
        if (key == GLFW_KEY_W) {
            position.add(front.mul(velocity, new Vector3f()));
        }
        if (key == GLFW_KEY_S) {
            position.sub(front.mul(velocity, new Vector3f()));
        }
        if (key == GLFW_KEY_A) {
            position.sub(right.mul(velocity, new Vector3f()));
        }
        if (key == GLFW_KEY_D) {
            position.add(right.mul(velocity, new Vector3f()));
        }
        if (key == GLFW_KEY_Q) {
            position.sub(up.mul(velocity, new Vector3f()));
        }
        if (key == GLFW_KEY_E) {
            position.add(up.mul(velocity, new Vector3f()));
        }
        updateViewMatrix();
    }

    public void processMouseMovement(float xOffset, float yOffset) {
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;

        yaw += xOffset;
        pitch += yOffset;

        // Constrain the pitch to avoid gimbal lock
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        // Normalize yaw to keep within 0 to 360 degrees
        while (yaw < 0) yaw += 360;
        while (yaw >= 360) yaw -= 360;

        updateCameraVectors();
    }

    public void processMouseScroll(float yOffset) {
        zoom -= yOffset;
        if (zoom < 40.0f) zoom = 40.0f;
        if (zoom > 90.0f) zoom = 90.0f;

        fov = zoom;
        updateProjectionMatrix();
    }


    private void updateCameraVectors() {
        // Calculate direction vector
        Vector3f direction = new Vector3f();
        direction.x = (float) Math.cos(Math.toRadians(yaw));
        direction.y = 0;
        direction.z = (float) Math.sin(Math.toRadians(yaw));
        front.set(direction).normalize();

        // Calculate the front vector without pitch
        Vector3f frontWithPitch = new Vector3f(front);
        frontWithPitch.y = (float) Math.sin(Math.toRadians(pitch));
        frontWithPitch.normalize();
        front.set(frontWithPitch);

        // Re-calculate the right vector
        right.set(front.cross(worldUp, new Vector3f())).normalize();

        // Reset the up vector to ensure it remains at Y-axis (0, 1, 0)
        up = new Vector3f(0, 1, 0);
    }
    public Vector3f getFront() {
        return front;
    }
    public Vector3f getUp() {
        return up;
    }
    public Vector3f getRight() {
        return right;
    }
    public Vector3f getWorldUp() {
        return worldUp;
    }
    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, position.add(front, new Vector3f()), up);
    }

    public Matrix4f getPerspectiveMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getOrthographicMatrix(float left, float right, float bottom, float top) {
        return new Matrix4f().ortho(left, right, bottom, top, nearPlane, farPlane);
    }

    private void updateViewMatrix() {
        viewMatrix.identity().lookAt(position, position.add(front, new Vector3f()), up);
    }

    private void updateProjectionMatrix() {
        projectionMatrix.identity().perspective((float) Math.toRadians(fov), aspectRatio, nearPlane, farPlane);
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
