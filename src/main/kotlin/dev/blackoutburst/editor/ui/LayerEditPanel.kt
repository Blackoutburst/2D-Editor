package dev.blackoutburst.editor.ui

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.ColoredBox2D
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.ui.ColorPicker
import dev.blackoutburst.bogel.ui.HuePicker
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.inputs.getScreenPosition
import dev.blackoutburst.editor.tiles.TileLayer

object LayerEditPanel {
    var layer: TileLayer? = null
        set(value) {
            field = value
            value?.let {
                colorPicker.color = value.color.color
            }
        }

    private const val MARGIN = 10f
    private const val PANEL_WIDTH = 200f
    private const val PANEL_HEIGHT = 400f

    private val background = ColoredBox2D(0f, 0f, PANEL_WIDTH + 10, PANEL_HEIGHT + 10, Color.DARK_GRAY, 10f)
    private val hueRotate = HuePicker(0f, 0f, 180f, 20f, 8f)
    private val colorPicker = ColorPicker(0f, 0f, 180f, 180f, 8f)

    fun update() {
        if (layer == null) return

        background.x = Window.width -PANEL_WIDTH - LayerPanel.PANEL_WIDTH -Camera.position.x
        background.y = -Camera.position.y - 10f


        colorPicker.x = Window.width -PANEL_WIDTH - LayerPanel.PANEL_WIDTH -Camera.position.x + MARGIN
        colorPicker.y = -Camera.position.y + PANEL_HEIGHT - 180f - MARGIN

        hueRotate.x = Window.width -PANEL_WIDTH - LayerPanel.PANEL_WIDTH -Camera.position.x + MARGIN
        hueRotate.y = -Camera.position.y + PANEL_HEIGHT - 180f - 30f - MARGIN

        if (Mouse.isButtonDown(Mouse.LEFT_BUTTON)) {
            colorPicker.color = hueRotate.selectColor(colorPicker.color)
            layer!!.color.color = colorPicker.selectColor(layer!!.color.color)
        }

        val mp = Mouse.getScreenPosition()
        if (mp.x >= background.x && mp.x <= background.x + PANEL_WIDTH && mp.y <= background.y + PANEL_HEIGHT && mp.y >= background.y) {
            Mouse.update()
        } else if (Mouse.isButtonPressed(Mouse.LEFT_BUTTON)) {
            layer = null
        }
    }

    fun render() {
        if (layer == null) return

        background.render()
        hueRotate.render()
        colorPicker.render()
    }
}