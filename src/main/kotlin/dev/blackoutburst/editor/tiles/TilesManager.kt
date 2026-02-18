package dev.blackoutburst.editor.tiles

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.TextureArray
import dev.blackoutburst.bogel.maths.Matrix
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.shader.Shader
import dev.blackoutburst.bogel.shader.ShaderProgram
import dev.blackoutburst.bogel.utils.stack
import dev.blackoutburst.editor.Main
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

object TilesManager {
    private val tiles = mutableListOf<Tile>()

    private val vertexShader = Shader(GL_VERTEX_SHADER, "/shaders/tiles.vert")
    private val fragmentShader = Shader(GL_FRAGMENT_SHADER, "/shaders/tiles.frag")
    private val shaderProgram = ShaderProgram(vertexShader, fragmentShader)

    private var diffuseMap: TextureArray? = null

    private val model = Matrix()

    private val vaoId = glGenVertexArrays()
    private val vboId = glGenBuffers()

    private var vertices = floatArrayOf()

    private val textureMap = mutableMapOf<String, Int>()

    fun refreshDiffuseMap() {
        diffuseMap?.let { glDeleteTextures(it.id) }

        textureMap.clear()

        Main.textureFolder.listFiles().toList().forEachIndexed { index, file ->
            textureMap[file.name] = index
        }

        diffuseMap = TextureArray(Main.textureFolder.listFiles().toList().map { it.canonicalPath }, fromJar = false)
    }

    private fun generate() {
        val vertexArray = mutableListOf<Float>()

        for (t in tiles) {
            vertexArray.addAll(listOf(textureMap[t.texture]?.toFloat() ?: 0f, t.position.x, t.position.y, 0.0f, 0.0f, t.color.r, t.color.g, t.color.b, t.color.a))
            vertexArray.addAll(listOf(textureMap[t.texture]?.toFloat() ?: 0f, t.position.x + t.size.x, t.position.y, 1.0f, 0.0f, t.color.r, t.color.g, t.color.b, t.color.a))
            vertexArray.addAll(listOf(textureMap[t.texture]?.toFloat() ?: 0f, t.position.x + t.size.x, t.position.y + t.size.y, 1.0f, 1.0f, t.color.r, t.color.g, t.color.b, t.color.a))

            vertexArray.addAll(listOf(textureMap[t.texture]?.toFloat() ?: 0f, t.position.x + t.size.x, t.position.y + t.size.y, 1.0f, 1.0f, t.color.r, t.color.g, t.color.b, t.color.a))
            vertexArray.addAll(listOf(textureMap[t.texture]?.toFloat() ?: 0f, t.position.x, t.position.y + t.size.y, 0.0f, 1.0f, t.color.r, t.color.g, t.color.b, t.color.a))
            vertexArray.addAll(listOf(textureMap[t.texture]?.toFloat() ?: 0f, t.position.x, t.position.y, 0.0f, 0.0f, t.color.r, t.color.g, t.color.b, t.color.a))
        }

        vertices = vertexArray.toFloatArray()

        stack(256 * 1024) { stack ->
            glBindVertexArray(vaoId)

            // VBO
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            val vertexBuffer = stack.mallocFloat(vertices.size)
            vertexBuffer.put(vertices).flip()
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

    fun addTile(tile: Tile) {
        tiles.add(tile)

        generate()
    }

    fun getTile(position: Vector2f): Tile? {
        for (t in tiles) {
            if (t.position.x == position.x && t.position.y == position.y) {
                return t
            }
        }

        return null
    }

    fun removeTile(tile: Tile) {
        getTile(tile.position)?.let { tiles.remove(it) }

        generate()
    }

    fun update() {}

    fun render() {
        glBindVertexArray(vaoId)
        glUseProgram(shaderProgram.id)

        diffuseMap?.let {
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D_ARRAY, it.id)
            shaderProgram.setUniform1i("diffuseMap", 0)
        }
        shaderProgram.setUniformMat4("view", Camera.view)
        shaderProgram.setUniformMat4("model", model)
        shaderProgram.setUniformMat4("projection", Camera.projection2D)

        glDrawArrays(GL_TRIANGLES, 0, vertices.size / 9)
    }
}