package dev.blackoutburst.editor.camera

import dev.blackoutburst.editor.input.Mouse
import dev.blackoutburst.editor.maths.Matrix
import dev.blackoutburst.editor.maths.Vector2f
import dev.blackoutburst.editor.maths.Vector3f
import dev.blackoutburst.editor.window.Window

object Camera {
    private var lastMousePosition = Mouse.position

    var position = Vector3f(0f, 0f, 5f)

    var rotation = Vector2f(40f, 30f)

    var view = Matrix().translate(position)
    var projection = Matrix().projectionMatrix(90f, 1000f, 0.1f)
    var projection2D = Matrix().ortho2D(0f, Window.width.toFloat(), 0f, Window.height.toFloat(), -1f, 1f)

    fun update() {
        val mousePosition = Mouse.position

        var xOffset = 0f
        var yOffset = 0f

        if (Mouse.isButtonDown(Mouse.MIDDLE_BUTTON)) {
            xOffset = mousePosition.x - lastMousePosition.x
            yOffset = mousePosition.y - lastMousePosition.y
        }

        lastMousePosition = mousePosition.copy()

        //position.z -= Mouse.scroll / 2f
        position.x += xOffset
        position.y -= yOffset

        view.setIdentity()
            .translate(position.x, position.y, 0f)
    }
}
