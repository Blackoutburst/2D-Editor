package dev.blackoutburst.editor.ui

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.ColoredBox2D
import dev.blackoutburst.bogel.input.Keyboard
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.inputs.getScreenPosition
import dev.blackoutburst.editor.tiles.TileLayer
import dev.blackoutburst.editor.tiles.TilesManager
import org.lwjgl.glfw.GLFW

object LayerPanel {
    private const val PANEL_WIDTH = 200f
    private const val MARGIN = 10f

    var selected: TileLayer? = TilesManager.layers.firstOrNull() ?: throw IllegalStateException("No layers please init after TileManager")

    private var visible = true

    private val background = ColoredBox2D(Window.width - PANEL_WIDTH, 0f, PANEL_WIDTH, Window.height.toFloat(), Color.DARK_GRAY)
    private val selectBox = ColoredBox2D(0f, 0f, 55f, 55f, Color.GRAY, 8f)

    fun update() {
        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_L))
            visible = !visible

        if (!visible) return

        background.height = Window.height.toFloat()
        background.x = Window.width - PANEL_WIDTH -Camera.position.x
        background.y = -Camera.position.y

        val mp = Mouse.getScreenPosition()
        if (mp.x >= Window.width - PANEL_WIDTH - Camera.position.x) { Mouse.update() }
    }

    fun render() {
        if (!visible) return

        background.render()
    }
}