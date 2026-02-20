package dev.blackoutburst.editor.ui

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.ColoredBox2D
import dev.blackoutburst.bogel.graphics.Text
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.ui.ColorPicker
import dev.blackoutburst.bogel.ui.HuePicker
import dev.blackoutburst.bogel.ui.TextField
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.inputs.getScreenPosition
import dev.blackoutburst.editor.tiles.TileLayer
import dev.blackoutburst.editor.tiles.TilesManager

object LayerEditPanel {
    var layer: TileLayer? = null
        set(value) {
            field = value
            value?.let {
                colorPicker.color = value.color.color
                layerNameField.inputText.text = value.name.text
                layerNameField.cursorPosition = value.name.text.length

                layerGridField.inputText.text = value.gridSize.toString()
                layerGridField.cursorPosition = value.gridSize.toString().length

                textureSizeField.inputText.text = value.textureSize.toString()
                textureSizeField.cursorPosition = value.textureSize.toString().length
            }
        }

    private const val MARGIN = 10f
    private const val PANEL_WIDTH = 200f
    private const val PANEL_HEIGHT = 420f

    private val background = ColoredBox2D(0f, 0f, PANEL_WIDTH + 10, PANEL_HEIGHT + 10, Color.DARK_GRAY, 10f)
    private val hueRotate = HuePicker(0f, 0f, 180f, 20f, 8f)
    private val colorPicker = ColorPicker(0f, 0f, 180f, 180f, 8f)

    private val layerNameText = Text(0f, 0f, 16f, "Layer name")
    private val layerNameField = TextField(
        x = 0f,
        y = 0f,
        width = 180f,
        height = 25f,
        borderRadius = 8f
    )

    private val layerGridText = Text(0f, 0f, 16f, "Grid size")
    private val layerGridField = TextField(
        x = 0f,
        y = 0f,
        width = 180f,
        height = 25f,
        borderRadius = 8f,
        numberOnly = true
    )

    private val textureSizeText = Text(0f, 0f, 16f, "Texture size")
    private val textureSizeField = TextField(
        x = 0f,
        y = 0f,
        width = 180f,
        height = 25f,
        borderRadius = 8f,
        numberOnly = true
    )

    fun update() {
        if (layer == null) return

        val elementX = Window.width -PANEL_WIDTH - LayerPanel.PANEL_WIDTH -Camera.position.x + MARGIN
        var elementY = -Camera.position.y + PANEL_HEIGHT - 180f - MARGIN

        background.x = Window.width -PANEL_WIDTH - LayerPanel.PANEL_WIDTH -Camera.position.x
        background.y = -Camera.position.y - 10f


        colorPicker.x = elementX
        colorPicker.y = elementY

        elementY -= 30f

        hueRotate.x = elementX
        hueRotate.y = elementY

        if (Mouse.isButtonDown(Mouse.LEFT_BUTTON)) {
            colorPicker.color = hueRotate.selectColor(colorPicker.color)
            layer!!.color.color = colorPicker.selectColor(layer!!.color.color)
        }

        elementY -= 30f

        // LAYER NAME
        layerNameText.x = elementX
        layerNameText.y = elementY

        elementY -= 30f

        layerNameField.x = elementX
        layerNameField.y = elementY

        layerNameField.onExit {
            layerNameField.backgroundColor = Color(0.1f)
            layerNameField.outlineColor = Color(0.2f)
        }
        layerNameField.onHover {
            layerNameField.backgroundColor = Color(0.2f)
            layerNameField.outlineColor = Color(0.3f)
        }
        layerNameField.onClick {}

        layerNameField.update {
            layer!!.name.text = layerNameField.inputText.text
        }

        elementY -= 30f

        // LAYER GRID
        layerGridText.x = elementX
        layerGridText.y = elementY

        elementY -= 30f

        layerGridField.x = elementX
        layerGridField.y = elementY

        layerGridField.onExit {
            layerGridField.backgroundColor = Color(0.1f)
            layerGridField.outlineColor = Color(0.2f)
        }
        layerGridField.onHover {
            layerGridField.backgroundColor = Color(0.2f)
            layerGridField.outlineColor = Color(0.3f)
        }
        layerGridField.onClick {}

        layerGridField.update {
            try {
                val size = layerGridField.inputText.text.toInt()
                layer!!.gridSize = size
            } catch (_: Exception) {
                layerGridField.inputText.text = layer!!.gridSize.toString()
                layerGridField.cursorPosition = layer!!.gridSize.toString().length
            }
        }

        // TEXTURE SIZE
        elementY -= 30f

        // LAYER GRID
        textureSizeText.x = elementX
        textureSizeText.y = elementY

        elementY -= 30f

        textureSizeField.x = elementX
        textureSizeField.y = elementY

        textureSizeField.onExit {
            textureSizeField.backgroundColor = Color(0.1f)
            textureSizeField.outlineColor = Color(0.2f)
        }
        textureSizeField.onHover {
            textureSizeField.backgroundColor = Color(0.2f)
            textureSizeField.outlineColor = Color(0.3f)
        }
        textureSizeField.onClick {}

        textureSizeField.update {
            try {
                val size = textureSizeField.inputText.text.toInt()
                layer!!.textureSize = size
                TilesManager.refreshDiffuseMap()
            } catch (_: Exception) {
                textureSizeField.inputText.text = layer!!.textureSize.toString()
                textureSizeField.cursorPosition = layer!!.textureSize.toString().length
            }
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

        layerNameText.render()
        layerNameField.render()

        layerGridText.render()
        layerGridField.render()

        textureSizeText.render()
        textureSizeField.render()
    }
}