package dev.blackoutburst.editor.tiles

import dev.blackoutburst.bogel.graphics.Framebuffer
import dev.blackoutburst.bogel.graphics.TextureArray
import dev.blackoutburst.bogel.utils.Color

data class TileLayer(
    var order: Int,
    var name: String,
    var color: Color,
    var visible: Boolean,
    val tiles: MutableList<Tile>,
    val framebuffer: Framebuffer,
    val gridSize: Int,
    var glVAO: Int,
    var glVBO: Int,
    var glVertices: FloatArray,
    val textureMap: MutableMap<String, Int>,
    var textureSize: Int,
    var diffuseMap: TextureArray?
)
