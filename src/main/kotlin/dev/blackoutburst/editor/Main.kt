package dev.blackoutburst.editor

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.file.FileExplorer
import dev.blackoutburst.bogel.input.Keyboard
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.Main.Companion.queue
import dev.blackoutburst.editor.Main.Companion.textureFolder
import dev.blackoutburst.editor.callbacks.DragAndDropCallback
import dev.blackoutburst.editor.camera.CameraController
import dev.blackoutburst.editor.files.FileManager
import dev.blackoutburst.editor.graphics.Axis
import dev.blackoutburst.editor.graphics.Grid
import dev.blackoutburst.editor.inputs.getScreenPositionAlign
import dev.blackoutburst.editor.tiles.Tile
import dev.blackoutburst.editor.tiles.TilesManager
import dev.blackoutburst.editor.ui.LayerPanel
import dev.blackoutburst.editor.ui.TilePanel
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL
import org.lwjgl.glfw.GLFW.GLFW_KEY_O
import org.lwjgl.glfw.GLFW.GLFW_KEY_S
import org.lwjgl.glfw.GLFW.glfwSetDropCallback
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

class Main {
    companion object {
        var gridSize = 100
        var dragMode = false
        val textureFolder = File("./EditorFiles/tileTextures")
        val queue: ConcurrentLinkedQueue<() -> Unit> = ConcurrentLinkedQueue()
    }
}

fun main() {
    Window.setTitle("2D Editor").setVsync(false)
    glfwSetDropCallback(Window.id, DragAndDropCallback())

    FileExplorer.init()

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
        while(queue.isNotEmpty()) queue.poll()?.invoke()

        LayerPanel.selected?.let {
            if (it.gridSize != Main.gridSize) {
                Main.gridSize = it.gridSize
                Grid.generate()
            }

        }

        Main.dragMode = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)

        LayerPanel.update()

        TilesManager.update()

        Grid.update()
        Axis.update()
        TilePanel.update()


        CameraController.update()

        if (Keyboard.isKeyPressed(GLFW_KEY_O)) {
            FileExplorer.pickFile("2D Editor file", "*.2de") {
                it?.let { queue.add {
                    FileManager.load(it)
                    TilesManager.refreshDiffuseMap()
                } }
            }
        }

        if (Keyboard.isKeyPressed(GLFW_KEY_S)) {
            FileExplorer.saveFile("2D Editor file", "*.2de") {
                it?.let { FileManager.save(it) }
            }
        }

        if (Mouse.isButtonPressed(Mouse.LEFT_BUTTON) || (Main.dragMode && Mouse.isButtonDown(Mouse.LEFT_BUTTON))) {
            val mp = Mouse.getScreenPositionAlign(Main.gridSize)

            LayerPanel.selected?.let { layer ->
                TilesManager.getTile(layer, Vector2f(mp.x, mp.y))?.let {
                    TilesManager.removeTile(layer, it)
                }

                TilePanel.selected?.let { tile ->
                    TilesManager.addTile(
                        layer,
                        Tile(layer.order, tile, Vector2f(mp.x, mp.y), Vector2f(Main.gridSize.toFloat()), Color.WHITE)
                    )
                }
            }
        }

        if (Mouse.isButtonPressed(Mouse.RIGHT_BUTTON) || (Main.dragMode && Mouse.isButtonDown(Mouse.RIGHT_BUTTON))) {
            val mp = Mouse.getScreenPositionAlign(Main.gridSize)

            LayerPanel.selected?.let { layer ->
                TilesManager.getTile(layer, Vector2f(mp.x, mp.y))?.let {
                    TilesManager.removeTile(layer, it)
                }
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
        LayerPanel.render()

        Window.update()
    }

    Window.destroy()
}