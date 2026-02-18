package dev.blackoutburst.editor

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.Text
import dev.blackoutburst.bogel.input.Keyboard
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.Main.Companion.textureFolder
import dev.blackoutburst.editor.callbacks.DragAndDropCallback
import dev.blackoutburst.editor.camera.CameraController
import dev.blackoutburst.editor.graphics.Axis
import dev.blackoutburst.editor.graphics.Grid
import dev.blackoutburst.editor.inputs.getScreenPositionAlign
import dev.blackoutburst.editor.tiles.Tile
import dev.blackoutburst.editor.tiles.TilesManager
import dev.blackoutburst.editor.ui.TilePanel
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwSetDropCallback
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.io.File

class Main {
    companion object {
        val textureFolder = File("./EditorFiles/tileTextures")
    }
}

fun main() {
    Window.setTitle("2D Editor").setVsync(false)
    glfwSetDropCallback(Window.id, DragAndDropCallback())

    textureFolder.mkdirs()

    Camera.position.x = Window.width / 2f
    Camera.position.y = Window.height / 2f

    while (Window.isOpen) {
        update()
    }

}

fun update() {

    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glEnable(GL_TEXTURE_2D)
    glEnable(GL_TEXTURE_2D_ARRAY)

    TilesManager.refreshDiffuseMap()
    TilePanel.refreshPanel()

    while (Window.isOpen) {
        TilesManager.update()

        Grid.update()
        Axis.update()
        TilePanel.update()

        CameraController.update()
        if (Mouse.isButtonPressed(Mouse.LEFT_BUTTON)) {
            val mp = Mouse.getScreenPositionAlign(100)

            TilesManager.getTile(Vector2f(mp.x, mp.y))?.let {
                TilesManager.removeTile(it)
            }

            TilesManager.addTile(
                Tile(TilePanel.selected ?: "", Vector2f(mp.x, mp.y), Vector2f(100f), Color.WHITE)
            )
        }
        if (Mouse.isButtonPressed(Mouse.RIGHT_BUTTON)) {
            val mp = Mouse.getScreenPositionAlign(100)

            TilesManager.getTile(Vector2f(mp.x, mp.y))?.let {
                TilesManager.removeTile(it)
            }
        }

        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_R)) {
            TilesManager.refreshDiffuseMap()
            TilePanel.refreshPanel()
        }
        Window.clear()

        TilesManager.render()

        Grid.render()
        Axis.render()

        TilePanel.render()

        Window.update()
    }

    Window.destroy()
}