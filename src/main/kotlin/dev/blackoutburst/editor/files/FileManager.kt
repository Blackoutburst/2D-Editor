package dev.blackoutburst.editor.files

import dev.blackoutburst.bogel.graphics.ColoredBox2D
import dev.blackoutburst.bogel.graphics.Text
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.editor.tiles.Tile
import dev.blackoutburst.editor.tiles.TileLayer
import dev.blackoutburst.editor.tiles.TilesManager
import java.io.File
import java.io.PrintWriter

object FileManager {

    fun save(filePath: String) {
        PrintWriter(filePath).use { writer ->
            TilesManager.layers.forEach {
                writer.println("L|${it.name.text}|${it.gridSize}|${it.textureSize}|${it.color.color.r}|${it.color.color.g}|${it.color.color.b}|${it.color.color.a}|${it.visible}")

                it.tiles.forEach { tile ->
                    writer.println("T|${tile.layer}|${tile.texture}|${tile.position.x}|${tile.position.y}|${tile.size.x}|${tile.size.y}|${tile.color.r}|${tile.color.g}|${tile.color.b}|${tile.color.a}")
                }
            }
        }
    }

    fun load(filePath: String) {
        TilesManager.layers.clear()

        var layerIndex = 0
        File(filePath).forEachLine { line ->
            val split = line.split("|")
            if (line.startsWith("L")) {
                val layer = TileLayer(
                    order = layerIndex,
                    name = Text(0f, 0f, 16f, split[1]),
                    gridSize = split[2].toInt(),
                    textureSize = split[3].toInt(),
                    color = ColoredBox2D(
                        0f,
                        0f,
                        0f,
                        0f,
                        Color(split[4].toFloat(), split[5].toFloat(), split[6].toFloat(), split[7].toFloat()),
                        8f
                    ),
                    visible = split[8].toBoolean(),
                )
                TilesManager.layers.add(layer)
                layerIndex++
            }
            if (line.startsWith("T")) {
                TilesManager.layers[split[1].toInt()].let {
                    TilesManager.addTile(
                        it,
                        Tile(
                            layer = split[1].toInt(),
                            texture = split[2],
                            position = Vector2f(split[3].toFloat(), split[4].toFloat()),
                            size = Vector2f(split[5].toFloat(), split[6].toFloat()),
                            color = Color(split[7].toFloat(), split[8].toFloat(), split[9].toFloat(), split[10].toFloat())
                        )
                    )
                }
            }
        }
    }
}