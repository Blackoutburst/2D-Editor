package dev.blackoutburst.editor.ui

import dev.blackoutburst.bogel.camera.Camera
import dev.blackoutburst.bogel.graphics.ColoredBox2D
import dev.blackoutburst.bogel.graphics.Texture
import dev.blackoutburst.bogel.input.Keyboard
import dev.blackoutburst.bogel.input.Mouse
import dev.blackoutburst.bogel.maths.Matrix
import dev.blackoutburst.bogel.maths.Vector2f
import dev.blackoutburst.bogel.shader.Shader
import dev.blackoutburst.bogel.shader.ShaderProgram
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.utils.stack
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.Main
import dev.blackoutburst.editor.inputs.getScreenPosition
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

object TilePanel {

    var selected = 0

    private data class TileElement(
        var x: Float,
        var y: Float,
        val textureId: Int,
        var alpha: Float,
    )

    private var visible = true

    private const val TILE_SIZE = 50f
    private const val MARGIN = 10f

    private val vertices = floatArrayOf(
        0f, 0f, 0f, 0f,
        1f, 1f, 1f, 1f,
        0f, 1f, 0f, 1f,
        1f, 0f, 1f, 0f,
    )

    private val indices = intArrayOf(
        0, 1, 2,
        0, 3, 1,
    )

    private var vaoID = 0

    private var model = Matrix()

    private var tiles = listOf<TileElement>()

    private val vertexShader = Shader(GL_VERTEX_SHADER, "/shaders/2D.vert")
    private val fragmentShader = Shader(GL_FRAGMENT_SHADER, "/shaders/2D.frag")
    private val shaderProgram = ShaderProgram(vertexShader, fragmentShader)

    private val background = ColoredBox2D(0f, 0f, 250f, Window.height.toFloat(), Color.DARK_GRAY)
    private val selectBox = ColoredBox2D(0f, 0f, 55f, 55f, Color.WHITE)

    init {
        vaoID = glGenVertexArrays()
        val vboID = glGenBuffers()
        val eboID = glGenBuffers()

        stack { stack ->
            glBindVertexArray(vaoID)

            glBindBuffer(GL_ARRAY_BUFFER, vboID)
            val vertexBuffer = stack.mallocFloat(vertices.size)
            vertexBuffer.put(vertices).flip()
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
            val indexBuffer = stack.mallocInt(indices.size)
            indexBuffer.put(indices).flip()
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)

            glEnableVertexAttribArray(0)
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0)
            glEnableVertexAttribArray(1)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 16, 8)
            glBindVertexArray(0)
        }
    }

    fun refreshPanel() {
        var x = MARGIN
        var y = Window.height - TILE_SIZE - MARGIN

        tiles = Main.textureFolder.listFiles().toList().map { it.canonicalPath }.mapIndexed { index, path ->
            if (index > 0) {
                if (index % 4 == 0) {
                    y -= TILE_SIZE + MARGIN
                    x = MARGIN
                } else {
                    x += (TILE_SIZE + MARGIN)
                }
            }

            TileElement(
                x,
                y,
                Texture(path, fromJar = false).id,
                0.5f,
            )
        }
    }

    fun update() {
        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_T))
            visible = !visible

        if (!visible) return

        background.height = Window.height.toFloat()
        background.x = -Camera.position.x
        background.y = -Camera.position.y

        val mp = Mouse.getScreenPosition()
        tiles.forEachIndexed { index, tile ->
            if (selected == index) {
                selectBox.x = tile.x - 2.5f
                selectBox.y = tile.y - 2.5f
            }

            if (mp.x >= tile.x && mp.x <= tile.x + TILE_SIZE && mp.y >= tile.y && mp.y <= tile.y + TILE_SIZE) {
                if (Mouse.isButtonPressed(Mouse.LEFT_BUTTON))
                    selected = index

                tile.alpha = 1f
            } else {
                tile.alpha = if (selected == index) 1.0f else 0.5f
            }
        }

        if (mp.x <= 250) { Mouse.update() }
    }

    fun render() {
        if (!visible) return

        background.render()
        selectBox.render()

        tiles.forEach {

            glUseProgram(shaderProgram.id)

            shaderProgram.setUniform1i("diffuseMap", 0)
            shaderProgram.setUniformMat4("model", model.setIdentity().translate(Vector2f(-Camera.position.x + it.x, -Camera.position.y + it.y)).scale(Vector2f(TILE_SIZE)))
            shaderProgram.setUniformMat4("projection", Camera.projection2D)
            shaderProgram.setUniformMat4("view", Camera.view)
            shaderProgram.setUniform1f("alpha", it.alpha)

            glActiveTexture(GL_TEXTURE0)
            glBindVertexArray(vaoID)
            glBindTexture(GL_TEXTURE_2D, it.textureId)
            glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
        }
    }
}