package dev.blackoutburst.editor.inputs

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.window.Window
import kotlin.math.floor

fun Mouse.getScreenPosition(): Vector2f {
    val x = -Camera.position.x + Mouse.position.x
    val y = -Camera.position.y + (Window.height - Mouse.position.y)

    return Vector2f(x, y)
}

fun Mouse.getScreenPositionAlign(gridSize: Int): Vector2f {
    val x = floor((-Camera.position.x + Mouse.position.x) / gridSize) * gridSize
    val y = floor((-Camera.position.y + (Window.height - Mouse.position.y)) / gridSize) * gridSize

    return Vector2f(x, y)
}