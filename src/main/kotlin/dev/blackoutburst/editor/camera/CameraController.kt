package dev.blackoutburst.editor.camera

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.input.Keyboard
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.maths.Vector2f
import org.lwjgl.glfw.GLFW

object CameraController {

    private const val SPEED = 2

    private val previousMousePosition = Vector2f()

    init {
        previousMousePosition.set(Mouse.position.x, Mouse.position.y)
    }

    fun update() {
        var dx = 0f
        var dy = 0f

        if (Mouse.isButtonDown(Mouse.MIDDLE_BUTTON)) {
            dx = Mouse.position.x - previousMousePosition.x
            dy = Mouse.position.y - previousMousePosition.y
        }

        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_UP)) dy = 1f * SPEED
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN)) dy = -1f * SPEED
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) dx = 1f * SPEED
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT)) dx = -1f * SPEED

        Camera.position.x += dx
        Camera.position.y -= dy

        Camera.view.setIdentity()
            .translate(Camera.position.x, Camera.position.y, Camera.position.z)

        previousMousePosition.set(Mouse.position.x, Mouse.position.y)
    }
}