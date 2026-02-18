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
import dev.blackoutburst.bogel.ui.Button
import dev.blackoutburst.bogel.utils.Color
import dev.blackoutburst.bogel.utils.stack
import dev.blackoutburst.bogel.window.Window
import dev.blackoutburst.editor.Main
import dev.blackoutburst.editor.inputs.getScreenPosition
import dev.blackoutburst.editor.tiles.TilesManager
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays

object TilePanel {

    private const val PANEL_WIDTH = 250f
    private const val TILE_SIZE = 50f
    private const val MARGIN = 10f

    var selected: String? = Main.textureFolder.listFiles().first().name

    private val textureMap = mutableMapOf<String, Int>()

    private data class TileElement(
        var x: Float = 0f,
        var y: Float = 0f,
        val textureId: Int,
        val textureName: String,
        var alpha: Float = 0.5f,
    )

    private var visible = true

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

    private val background = ColoredBox2D(0f, 0f, PANEL_WIDTH, Window.height.toFloat(), Color.DARK_GRAY)
    private val selectBox = ColoredBox2D(0f, 0f, 55f, 55f, Color.GRAY, 8f)
    private val refreshButton = Button(0f, 0f, 100f, 25f, "Refresh", 8f)

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
        tiles.forEach { glDeleteTextures(it.textureId) }

        textureMap.clear()

        Main.textureFolder.listFiles().toList().filter { !it.isHidden }.forEach {
            textureMap[it.name] = 0
        }

        tiles = Main.textureFolder.listFiles().toList().filter { !it.isHidden }.map { file ->
            TileElement(
                textureId = Texture(file.canonicalPath, fromJar = false).id,
                textureName = file.name,
            )
        }
    }

    fun update() {
        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_T))
            visible = !visible

        if (!visible) return

        if (textureMap[selected] == null) {
            selected = textureMap.keys.firstOrNull()
        }

        background.height = Window.height.toFloat()
        background.x = -Camera.position.x
        background.y = -Camera.position.y

        refreshButton.x = -Camera.position.x + MARGIN
        refreshButton.y = -Camera.position.y + Window.height - 25f - MARGIN


        refreshButton.onExit {
            refreshButton.backgroundColor = Color(0.1f)
            refreshButton.outlineColor = Color(0.2f)
        }
        refreshButton.onHover {
            refreshButton.backgroundColor = Color(0.2f)
            refreshButton.outlineColor = Color(0.3f)
        }
        refreshButton.onClick {
            TilesManager.refreshDiffuseMap()
            refreshPanel()
        }


        val mp = Mouse.getScreenPosition()
        var tx = MARGIN
        var ty = Window.height - TILE_SIZE - MARGIN - 50f
        tiles.forEachIndexed { index, tile ->
            if (index > 0) {
                if (index % 4 == 0) {
                    ty -= TILE_SIZE + MARGIN
                    tx = MARGIN
                } else {
                    tx += (TILE_SIZE + MARGIN)
                }
            }

            tile.x = tx
            tile.y = ty

            if (selected == tile.textureName) {
                selectBox.x = tile.x - 2.5f - Camera.position.x
                selectBox.y = tile.y - 2.5f - Camera.position.y
            }

            if (mp.x >= tile.x - Camera.position.x && mp.x <= tile.x + TILE_SIZE - Camera.position.x && mp.y >= tile.y - Camera.position.y && mp.y <= tile.y + TILE_SIZE - Camera.position.y) {
                if (Mouse.isButtonPressed(Mouse.LEFT_BUTTON))
                    selected = tile.textureName

                tile.alpha = 1f
            } else {
                tile.alpha = if (selected == tile.textureName) 1.0f else 0.5f
            }
        }

        if (mp.x <= PANEL_WIDTH - Camera.position.x) { Mouse.update() }
    }

    fun render() {
        if (!visible) return

        background.render()

        refreshButton.render()

        selected?.let { selectBox.render() }

        tiles.forEach {

            glUseProgram(shaderProgram.id)

            shaderProgram.setUniform1i("diffuseMap", 0)
            shaderProgram.setUniformMat4("model", model.setIdentity().translate(Vector2f(-Camera.position.x + it.x, -Camera.position.y + it.y)).scale(Vector2f(TILE_SIZE)))
            shaderProgram.setUniformMat4("projection", Camera.projection2D)
            shaderProgram.setUniformMat4("view", Camera.view)
            shaderProgram.setUniform1f("alpha", it.alpha)
            shaderProgram.setUniform1f("borderRadius", 8f)
            shaderProgram.setUniform2f("size", TILE_SIZE, TILE_SIZE)

            glActiveTexture(GL_TEXTURE0)
            glBindVertexArray(vaoID)
            glBindTexture(GL_TEXTURE_2D, it.textureId)
            glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
        }

    }
}