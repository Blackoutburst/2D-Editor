package dev.blackoutburst.editor.tiles

import dev.blackoutburst.bogel.graphics.ColoredBox2D
import dev.blackoutburst.bogel.graphics.Framebuffer
import dev.blackoutburst.bogel.graphics.Text
import dev.blackoutburst.bogel.graphics.TextureArray
import dev.blackoutburst.bogel.ui.Button
import dev.blackoutburst.bogel.utils.Color
import org.lwjgl.opengl.GL15.glGenBuffers
import org.lwjgl.opengl.GL30.glGenVertexArrays

data class TileLayer(
    var order: Int,
    var name: Text,
    var color: ColoredBox2D = ColoredBox2D(0f, 0f, 0f, 0f, Color.RED, 8f),
    var visible: Boolean = true,
    val tiles: MutableList<Tile> = mutableListOf(),
    val framebuffer: Framebuffer = Framebuffer(1600, 900),
    var gridSize: Int = 100,
    var glVAO: Int = glGenVertexArrays(),
    var glVBO: Int = glGenBuffers(),
    var glVertices: FloatArray = floatArrayOf(),
    val textureMap: MutableMap<String, Int> = mutableMapOf(),
    var textureSize: Int = 16,
    var diffuseMap: TextureArray? = null,
    val editButton: Button = Button(0f, 0f, 50f, 25f, "Edit", 8f),
    val visibilityButton: Button = Button(0f, 0f, 15f, 15f, "", borderRadius = 100f, initialOutlineSize = 2f, initialOutlineColor = Color.DARK_GRAY, initialBackgroundColor = if (visible) Color.GREEN else Color.RED),
)
