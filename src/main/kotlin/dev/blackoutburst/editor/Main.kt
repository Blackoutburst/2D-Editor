package dev.blackoutburst.editor

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.editor.graphics.Axis
import dev.blackoutburst.editor.graphics.Grid
import dev.blackoutburst.bogel.graphics.Text
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.camera.CameraController
import dev.blackoutburst.editor.inputs.getScreenPositionAlign
import dev.blackoutburst.editor.tiles.Tile
import dev.blackoutburst.editor.tiles.TilesManager
import org.lwjgl.opengl.GL11.*
import java.util.Random
import kotlin.math.floor

fun main() {
    Window.setTitle("2D Editor")
    update()
}

fun update() {
    val rng = Random()

    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    val text = Text(0f, 0f, 32f, "Hello World")

    TilesManager.addTile(Tile(0, Vector2f(), Vector2f(100f), Color.GRAY))

    while (Window.isOpen) {
        TilesManager.update()

        Grid.update()
        Axis.update()

        CameraController.update()

        if (Mouse.isButtonPressed(Mouse.LEFT_BUTTON)) {
            val mp = Mouse.getScreenPositionAlign(100)

            TilesManager.addTile(
                Tile(0, Vector2f(mp.x, mp.y), Vector2f(100f), Color(rng.nextFloat(), rng.nextFloat(), rng.nextFloat()))
            )
        }

        if (Mouse.isButtonPressed(Mouse.RIGHT_BUTTON)) {
            val mp = Mouse.getScreenPositionAlign(100)

            TilesManager.getTile(Vector2f(mp.x, mp.y))?.let {
                TilesManager.removeTile(it)
            }
        }

        Window.clear()

        TilesManager.render()

        Grid.render()
        Axis.render()

        text.render()

        Window.update()
    }
    Window.destroy()
}