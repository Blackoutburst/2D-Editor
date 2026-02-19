package dev.blackoutburst.editor.ui

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.ColoredBox2D
import dev.blackoutburst.bogel.graphics.Text
import dev.blackoutburst.bogel.input.Keyboard
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.ui.Button
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.inputs.getScreenPosition
import dev.blackoutburst.editor.tiles.TileLayer
import dev.blackoutburst.editor.tiles.TilesManager
import org.lwjgl.glfw.GLFW

object LayerPanel {
    const val PANEL_WIDTH = 200f
    private const val OUTLINE_SIZE = 10f
    private const val LAYER_WIDTH = 170f
    private const val LAYER_HEIGHT = 90f
    private const val MARGIN = 15f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BUTTON = 10f

    var selected: TileLayer? = TilesManager.layers.firstOrNull() ?: throw IllegalStateException("No layers please init after TileManager")

    private var visible = true

    private val newLayerButton = Button(0f, 0f, 100f, 25f, "Add Layer", 8f)
    private val background = ColoredBox2D(Window.width - PANEL_WIDTH, 0f, PANEL_WIDTH, Window.height.toFloat(), Color.DARK_GRAY)
    private val selectBox = ColoredBox2D(0f, 0f, LAYER_WIDTH + OUTLINE_SIZE, LAYER_HEIGHT + OUTLINE_SIZE, Color.GRAY, 10f)

    fun update() {
        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_L)) {
            LayerEditPanel.layer = null
            visible = !visible
        }

        if (!visible) return

        val mp = Mouse.getScreenPosition()

        newLayerButton.x = Window.width - PANEL_WIDTH -Camera.position.x + MARGIN_BUTTON
        newLayerButton.y = -Camera.position.y + Window.height - 25f - MARGIN_BUTTON

        newLayerButton.onExit {
            newLayerButton.backgroundColor = Color(0.1f)
            newLayerButton.outlineColor = Color(0.2f)
        }
        newLayerButton.onHover {
            newLayerButton.backgroundColor = Color(0.2f)
            newLayerButton.outlineColor = Color(0.3f)
        }
        newLayerButton.onClick {
            val layerCount = TilesManager.layers.size
            val newLayer = TileLayer(
                order = layerCount,
                name = Text(0f, 0f, 16f, "Layer (${layerCount})"),
            )

            TilesManager.layers.add(newLayer)

            TilesManager.refreshDiffuseMap()

            TilesManager.layers = TilesManager.layers.sortedBy { it.order }.toMutableList()
            selected = newLayer
        }

        background.height = Window.height.toFloat()
        background.x = Window.width - PANEL_WIDTH -Camera.position.x
        background.y = -Camera.position.y

        selectBox.x = Window.width - PANEL_WIDTH -Camera.position.x + MARGIN - (OUTLINE_SIZE / 2)
        var sy = Window.height - MARGIN - LAYER_HEIGHT - MARGIN_TOP
        for (layer in TilesManager.layers) {
            if (mp.x >= layer.color.x && mp.x <= layer.color.x + LAYER_WIDTH && mp.y >= layer.color.y && mp.y <= layer.color.y + LAYER_HEIGHT) {
                if (Mouse.isButtonPressed(Mouse.LEFT_BUTTON))
                    selected = layer
            }

            layer.color.x = Window.width - PANEL_WIDTH -Camera.position.x + MARGIN - (OUTLINE_SIZE / 8)
            layer.color.y = sy - Camera.position.y - (OUTLINE_SIZE / 8)
            layer.color.width = LAYER_WIDTH + (OUTLINE_SIZE / 4)
            layer.color.height = LAYER_HEIGHT + (OUTLINE_SIZE / 4)

            layer.name.x = Window.width - PANEL_WIDTH -Camera.position.x + MARGIN
            layer.name.y = sy - Camera.position.y

            layer.editButton.x = Window.width - PANEL_WIDTH -Camera.position.x + MARGIN + LAYER_WIDTH - 50f - (MARGIN_BUTTON / 2)
            layer.editButton.y = sy - Camera.position.y + LAYER_HEIGHT - 25f - (MARGIN_BUTTON / 2)

            layer.editButton.onExit {
                layer.editButton.backgroundColor = Color(0.1f)
                layer.editButton.outlineColor = Color(0.2f)
            }
            layer.editButton.onHover {
                layer.editButton.backgroundColor = Color(0.2f)
                layer.editButton.outlineColor = Color(0.3f)
            }
            layer.editButton.onClick {
                LayerEditPanel.layer = layer
            }


            layer.visibilityButton.x = Window.width - PANEL_WIDTH -Camera.position.x + MARGIN + (MARGIN_BUTTON / 2)
            layer.visibilityButton.y = sy - Camera.position.y + LAYER_HEIGHT - 15f - (MARGIN_BUTTON / 2)

            layer.visibilityButton.onExit {
                layer.visibilityButton.outlineColor = Color.DARK_GRAY
            }
            layer.visibilityButton.onHover {
                layer.visibilityButton.outlineColor = Color.GRAY
            }
            layer.visibilityButton.onClick {
                layer.visible = !layer.visible
                layer.visibilityButton.backgroundColor = if (layer.visible) Color.GREEN else Color.RED
            }

            if (layer == selected) {
                selectBox.y = sy - Camera.position.y - (OUTLINE_SIZE / 2)
            }
            sy -= LAYER_HEIGHT + MARGIN
        }

        if (mp.x >= background.x) { Mouse.update() }

        LayerEditPanel.update()
    }

    fun render() {
        if (!visible) return

        LayerEditPanel.render()

        background.render()
        newLayerButton.render()

        if (selected != null)
            selectBox.render()

        var y = Window.height - MARGIN - LAYER_HEIGHT - MARGIN_TOP
        for (layer in TilesManager.layers) {
            layer.color.render()
            layer.framebuffer.render(Window.width - PANEL_WIDTH -Camera.position.x + MARGIN, y - Camera.position.y, LAYER_WIDTH, LAYER_HEIGHT, borderRadius = 8f)
            y -= LAYER_HEIGHT + MARGIN

            layer.editButton.render()
            layer.visibilityButton.render()
            layer.name.render()
        }
    }
}