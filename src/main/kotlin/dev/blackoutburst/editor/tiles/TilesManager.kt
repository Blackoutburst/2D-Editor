package dev.blackoutburst.editor.tiles

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.Text
import dev.blackoutburst.bogel.graphics.Texture
import dev.blackoutburst.bogel.graphics.TextureArray
import dev.blackoutburst.bogel.maths.Matrix
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.shader.Shader
import dev.blackoutburst.bogel.shader.ShaderProgram
import dev.blackoutburst.bogel.utils.stack
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.Main
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.Platform

object TilesManager {
    private val missingTexture = Texture("textures/error.png")

    var layers = mutableListOf(
        TileLayer(
            order = 0,
            name = Text(0f, 0f, 16f, "default"),
        )
    )

    private val vertexShader = Shader(GL_VERTEX_SHADER, "/shaders/tiles.vert")
    private val fragmentShader = Shader(GL_FRAGMENT_SHADER, "/shaders/tiles.frag")
    private val shaderProgram = ShaderProgram(vertexShader, fragmentShader)

    private val model = Matrix()
    private val originView = Matrix()
    private val originProjection = Matrix()

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
                    size = layer.textureSize,
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

    fun addTile(layer: TileLayer, tile: Tile) {
        layer.tiles.add(tile)

        generate(layer)
    }

    fun getTile(layer: TileLayer, position: Vector2f): Tile? {
        for (t in layer.tiles) {
            if (t.position.x == position.x && t.position.y == position.y) {
                return t
            }
        }

        return null
    }

    fun removeTile(layer: TileLayer, tile: Tile) {
        getTile(layer, tile.position)?.let { tile ->
            layer.tiles.remove(tile)
            generate(layer)
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

            shaderProgram.setUniformMat4("projection", originProjection.setIdentity().ortho2D(
                -layer.framebuffer.width / 2f,
                layer.framebuffer.width / 2f,
                -layer.framebuffer.height / 2f,
                layer.framebuffer.height / 2f,
                -1f, 1f
            ))
            shaderProgram.setUniformMat4("view", originView)

            generateLayerFramebuffer(layer)

            shaderProgram.setUniformMat4("projection", Camera.projection2D)
            shaderProgram.setUniformMat4("view", Camera.view)

            if (layer.visible)
                glDrawArrays(GL_TRIANGLES, 0, layer.glVertices.size / 9)
        }
    }

    private fun generateLayerFramebuffer(layer: TileLayer) {
        val scale = if (Platform.get() == Platform.MACOSX) 2 else 1

        glViewport(0, 0, layer.framebuffer.width, layer.framebuffer.height)
        glBindFramebuffer(GL_FRAMEBUFFER, layer.framebuffer.fbo)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glDrawArrays(GL_TRIANGLES, 0, layer.glVertices.size / 9)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, Window.width * scale, Window.height * scale)
    }
}