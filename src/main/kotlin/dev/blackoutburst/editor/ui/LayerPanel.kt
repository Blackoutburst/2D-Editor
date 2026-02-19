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
    private const val OUTLINE_SIZE = 10f
    private const val LAYER_WIDTH = 170f
    private const val LAYER_HEIGHT = 90f
    private const val MARGIN = 15f

    var selected: TileLayer? = TilesManager.layers.firstOrNull() ?: throw IllegalStateException("No layers please init after TileManager")

    private var visible = true

    private val background = ColoredBox2D(Window.width - PANEL_WIDTH, 0f, PANEL_WIDTH, Window.height.toFloat(), Color.DARK_GRAY)
    private val selectBox = ColoredBox2D(0f, 0f, LAYER_WIDTH + OUTLINE_SIZE, LAYER_HEIGHT + OUTLINE_SIZE, Color.GRAY, 8f)

    fun update() {
        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_L))
            visible = !visible

        if (!visible) return

        background.height = Window.height.toFloat()
        background.x = Window.width - PANEL_WIDTH -Camera.position.x
        background.y = -Camera.position.y

        selectBox.x = Window.width - PANEL_WIDTH -Camera.position.x + MARGIN - (OUTLINE_SIZE / 2)
        var sy = Window.height - MARGIN - LAYER_HEIGHT
        for (layer in TilesManager.layers) {
            layer.colorOutline.x = Window.width - PANEL_WIDTH -Camera.position.x + MARGIN - (OUTLINE_SIZE / 8)
            layer.colorOutline.y = sy - Camera.position.y - (OUTLINE_SIZE / 8)
            layer.colorOutline.width = LAYER_WIDTH + (OUTLINE_SIZE / 4)
            layer.colorOutline.height = LAYER_HEIGHT + (OUTLINE_SIZE / 4)
            layer.colorOutline.color = layer.color

            if (layer.name == selected?.name) {
                selectBox.y = sy - Camera.position.y - (OUTLINE_SIZE / 2)
            }
            sy += 100f + MARGIN
        }

        val mp = Mouse.getScreenPosition()
        if (mp.x >= Window.width - PANEL_WIDTH - Camera.position.x) { Mouse.update() }
    }

    fun render() {
        if (!visible) return

        background.render()

        if (selected != null)
            selectBox.render()

        var y = Window.height - MARGIN - LAYER_HEIGHT
        for (layer in TilesManager.layers) {
            layer.colorOutline.render()
            layer.framebuffer.render(Window.width - PANEL_WIDTH -Camera.position.x + MARGIN, y - Camera.position.y, LAYER_WIDTH, LAYER_HEIGHT, borderRadius = 8f)
            y += 100f + MARGIN
        }
    }
}