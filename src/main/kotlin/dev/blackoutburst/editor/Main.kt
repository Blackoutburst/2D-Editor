package dev.blackoutburst.editor

import dev.blackoutburst.editor.camera.Camera
import dev.blackoutburst.editor.graphics.Grid
import dev.blackoutburst.editor.graphics.Text
import dev.blackoutburst.editor.window.Window
import org.lwjgl.opengl.GL11.*

fun main() {
    Window
    update()
}

fun update() {
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glEnable(GL_CULL_FACE)

    val text = Text(0f, 0f, 32f, "Hello World")

    while (Window.isOpen) {
        Grid.update()
        Camera.update()

        Window.clear()



        Grid.render()
        text.render()

        Window.update()
    }
    Window.destroy()
}