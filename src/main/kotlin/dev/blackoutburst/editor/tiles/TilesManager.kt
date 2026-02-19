package dev.blackoutburst.editor.tiles

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.Framebuffer
import dev.blackoutburst.bogel.graphics.Texture
import dev.blackoutburst.bogel.graphics.TextureArray
import dev.blackoutburst.bogel.maths.Matrix
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.shader.Shader
import dev.blackoutburst.bogel.shader.ShaderProgram
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.utils.stack
import dev.blackoutburst.editor.Main
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

object TilesManager {
    private val missingTexture = Texture("textures/error.png")

    private val layers = mutableListOf(
        TileLayer(
            name = "default",
            color = Color.RED,
            visible = true,
            tiles = mutableListOf(),
            framebuffer = Framebuffer(512, 512),
            gridSize = 100,
            glVAO = glGenVertexArrays(),
            glVBO = glGenBuffers(),
            glVertices = floatArrayOf(),
            textureMap = mutableMapOf(),
            textureSize = 16,
            diffuseMap = null
        )
    )

    private val vertexShader = Shader(GL_VERTEX_SHADER, "/shaders/tiles.vert")
    private val fragmentShader = Shader(GL_FRAGMENT_SHADER, "/shaders/tiles.frag")
    private val shaderProgram = ShaderProgram(vertexShader, fragmentShader)

    private val model = Matrix()
    private val originView = Matrix()

    fun refreshDiffuseMap() {
        for (layer in layers) {
            layer.diffuseMap?.let { glDeleteTextures(it.id) }

            layer.textureMap.clear()

            Main.textureFolder.listFiles().toList().filter { !it.isHidden }.forEachIndexed { index, file ->
                layer.textureMap[file.name] = index
            }

            layer.diffuseMap =
                TextureArray(
                    Main.textureFolder.listFiles().toList().filter { !it.isHidden }.map { it.canonicalPath },
                    fromJar = false
                )

            generate(layer)
        }
    }

    private fun generate(layer: TileLayer) {
        val vertexArray = mutableListOf<Float>()

        for (t in layer.tiles) {
            vertexArray.addAll(listOf(layer.textureMap[t.texture]?.toFloat() ?: -1f, t.position.x, t.position.y, 0.0f, 0.0f, t.color.r, t.color.g, t.color.b, t.color.a))
            vertexArray.addAll(listOf(layer.textureMap[t.texture]?.toFloat() ?: -1f, t.position.x + t.size.x, t.position.y, 1.0f, 0.0f, t.color.r, t.color.g, t.color.b, t.color.a))
            vertexArray.addAll(listOf(layer.textureMap[t.texture]?.toFloat() ?: -1f, t.position.x + t.size.x, t.position.y + t.size.y, 1.0f, 1.0f, t.color.r, t.color.g, t.color.b, t.color.a))

            vertexArray.addAll(listOf(layer.textureMap[t.texture]?.toFloat() ?: -1f, t.position.x + t.size.x, t.position.y + t.size.y, 1.0f, 1.0f, t.color.r, t.color.g, t.color.b, t.color.a))
            vertexArray.addAll(listOf(layer.textureMap[t.texture]?.toFloat() ?: -1f, t.position.x, t.position.y + t.size.y, 0.0f, 1.0f, t.color.r, t.color.g, t.color.b, t.color.a))
            vertexArray.addAll(listOf(layer.textureMap[t.texture]?.toFloat() ?: -1f, t.position.x, t.position.y, 0.0f, 0.0f, t.color.r, t.color.g, t.color.b, t.color.a))
        }

        layer.glVertices = vertexArray.toFloatArray()

        stack(256 * 1024) { stack ->
            glBindVertexArray(layer.glVAO)

            // VBO
            glBindBuffer(GL_ARRAY_BUFFER, layer.glVBO)
            val vertexBuffer = stack.mallocFloat(layer.glVertices.size)
            vertexBuffer.put(layer.glVertices).flip()
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

            // ATTRIB
            // TEXTURE ID
            glEnableVertexAttribArray(0)
            glVertexAttribPointer(0, 1, GL_FLOAT, false, 36, 0)

            // POSITION
            glEnableVertexAttribArray(1)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 36, 4)

            // UV
            glEnableVertexAttribArray(2)
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 36, 12)

            // COLOR
            glEnableVertexAttribArray(3)
            glVertexAttribPointer(3, 4, GL_FLOAT, false, 36, 20)
        }
    }

    fun addTile(layerName: String, tile: Tile) {
        layers.find { it.name == layerName }?.let {
            it.tiles.add(tile)

            generate(it)
        }
    }

    fun getTile(layerName: String, position: Vector2f): Tile? {
        layers.find { it.name == layerName }?.let {
            for (t in it.tiles) {
                if (t.position.x == position.x && t.position.y == position.y) {
                    return t
                }
            }
        }

        return null
    }

    fun removeTile(layerName: String, tile: Tile) {
        getTile(layerName, tile.position)?.let { tile ->
            layers.find { it.name == layerName }?.let { layer ->
                layer.tiles.remove(tile)
                generate(layer)
            }
        }
    }

    fun update() {}

    fun render() {
        for (layer in layers) {
            glBindVertexArray(layer.glVAO)
            glUseProgram(shaderProgram.id)

            layer.diffuseMap?.let {
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D_ARRAY, it.id)
                shaderProgram.setUniform1i("diffuseMap", 0)

            }

            glActiveTexture(GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D, missingTexture.id)
            shaderProgram.setUniform1i("missingDiffuseMap", 1)


            shaderProgram.setUniformMat4("model", model)
            shaderProgram.setUniformMat4("projection", Camera.projection2D)

            shaderProgram.setUniformMat4("view", originView)

            generateLayerFramebuffer(layer)

            shaderProgram.setUniformMat4("view", Camera.view)

            glDrawArrays(GL_TRIANGLES, 0, layer.glVertices.size / 9)
        }
    }

    private fun generateLayerFramebuffer(layer: TileLayer) {
        glBindFramebuffer(GL_FRAMEBUFFER, layer.framebuffer.fbo)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glDrawArrays(GL_TRIANGLES, 0, layer.glVertices.size / 9)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }
}