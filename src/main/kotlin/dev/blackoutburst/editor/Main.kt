package dev.blackoutburst.editor

import dev.blackoutburst.editor.camera.Camera
import dev.blackoutburst.editor.graphics.Grid
import dev.blackoutburst.editor.window.Window

fun main() {
    update()
}

fun update() {
    while (Window.isOpen) {
        Grid.update()
        Camera.update()

        Window.clear()

        Grid.render()

        Window.update()
    }
    Window.destroy()
}