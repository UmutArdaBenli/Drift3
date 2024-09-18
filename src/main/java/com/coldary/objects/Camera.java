package com.coldary.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;

    private float yaw;
    private float pitch;

    private float movementSpeed;
    private float mouseSensitivity;
    private float zoom;

    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        this.position = position;
        this.up = up;
        this.yaw = yaw;
        this.pitch = pitch;
        this.front = new Vector3f(0.0f, 0.0f, -1.0f);
        this.right = new Vector3f();
        this.movementSpeed = 2.5f;
        this.mouseSensitivity = 0.1f;
        this.zoom = 45.0f;

        // Default projection parameters
        this.fov = 45.0f;
        this.aspectRatio = 16.0f / 9.0f; // Assuming default aspect ratio
        this.nearPlane = 0.1f;
        this.farPlane = 100.0f;

        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, position.add(front, new Vector3f()), up);
    }

    public Matrix4f getPerspectiveMatrix() {
        return new Matrix4f().perspective((float)Math.toRadians(fov), aspectRatio, nearPlane, farPlane);
    }

    public Matrix4f getOrthographicMatrix(float left, float right, float bottom, float top) {
        return new Matrix4f().ortho(left, right, bottom, top, nearPlane, farPlane);
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
    }

    public void processMouseMovement(float xOffset, float yOffset) {
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;

        yaw += xOffset;
        pitch -= yOffset;

        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
        if (yaw > 360f) pitch = 0;
        if (yaw < -360f) pitch = -360f;

        updateCameraVectors();
    }

    public void processMouseScroll(float yOffset) {
        zoom -= yOffset;
        if (zoom < 1.0f) zoom = 1.0f;
        if (zoom > 45.0f) zoom = 45.0f;

        fov = zoom;
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

    private void updateCameraVectors() {
        Vector3f front = new Vector3f();
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        this.front = front.normalize();

        right = front.cross(up, new Vector3f()).normalize();
        up = right.cross(front, new Vector3f()).normalize();
    }
}