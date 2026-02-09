package dev.blackoutburst.editor.graphics

import dev.blackoutburst.editor.camera.Camera
import dev.blackoutburst.editor.input.Keyboard
import dev.blackoutburst.editor.maths.Matrix
import dev.blackoutburst.editor.maths.Vector2f
import dev.blackoutburst.editor.shader.Shader
import dev.blackoutburst.editor.shader.ShaderProgram
import dev.blackoutburst.editor.utils.stack
import dev.blackoutburst.editor.window.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import kotlin.math.ceil

object Grid {
    private const val SIZE = 100f

    private val position = Vector2f()

    private val vertexShader = Shader(GL_VERTEX_SHADER, "/shaders/grid.vert")
    private val fragmentShader = Shader(GL_FRAGMENT_SHADER, "/shaders/grid.frag")
    private val shaderProgram = ShaderProgram(vertexShader, fragmentShader)

    private val model = Matrix()

    private val vaoId = glGenVertexArrays()
    private val vboId = glGenBuffers()
    private val eboId = glGenBuffers()

    private var vertices: FloatArray? = null
    private var indices: IntArray? = null

    private var indexCount: Int = 0

    private var visible = true

    init {
        generate()

        stack(256 * 1024) { stack ->
            glBindVertexArray(vaoId)

            // VBO
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            val vertexBuffer = stack.mallocFloat(vertices?.size ?: 0)
            vertexBuffer.put(vertices).flip()
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

            // EBO
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId)
            val indexBuffer = stack.mallocInt(indices?.size ?: 0)
            indexBuffer.put(indices).flip()
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)

            // ATTRIB
            // POSITION
            glEnableVertexAttribArray(0)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 28, 0)

            // COLOR
            glEnableVertexAttribArray(1)
            glVertexAttribPointer(1, 4, GL_FLOAT, false, 28, 12)
        }

        indexCount = indices?.size ?: 0

        vertices = null
        indices = null
    }

    private fun generate() {
        val vertexArray = mutableListOf<Float>()
        val indexArray = mutableListOf<Int>()

        val width = ceil(Window.width.toFloat() / SIZE) * SIZE + (SIZE * 10)
        val height = ceil(Window.height.toFloat() / SIZE) * SIZE + (SIZE * 10)

        for (x in 0 .. 20) {
            vertexArray.addAll(listOf(SIZE * x.toFloat(), 0f, 0f, 0.2f, 0.2f, 0.2f, 1f))
            indexArray.add(vertexArray.size / 7 - 1)
            vertexArray.addAll(listOf(SIZE * x.toFloat(), height, 0f, 0.2f, 0.2f, 0.2f, 1f))
            indexArray.add(vertexArray.size / 7 - 1)
        }
        for (y in 0 .. 20) {
            vertexArray.addAll(listOf(0f, SIZE * y.toFloat(), 0f, 0.2f, 0.2f, 0.2f, 1f))
            indexArray.add(vertexArray.size / 7 - 1)
            vertexArray.addAll(listOf(width, SIZE * y.toFloat(), 0f, 0.2f, 0.2f, 0.2f, 1f))
            indexArray.add(vertexArray.size / 7 - 1)
        }

        vertices = vertexArray.toFloatArray()
        indices = indexArray.toIntArray()
    }

    fun update() {
        position.x = -ceil(Camera.position.x / SIZE) * SIZE
        position.y = -ceil(Camera.position.y / SIZE) * SIZE

        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_G))
            visible = !visible
    }

    fun render() {
        if (!visible) return

        model.setIdentity().translate(position.x, position.y)


        glBindVertexArray(vaoId)
        glUseProgram(shaderProgram.id)
        shaderProgram.setUniformMat4("view", Camera.view)
        shaderProgram.setUniformMat4("model", model)
        shaderProgram.setUniformMat4("projection", Camera.projection2D)

        glDrawElements(GL_LINES, indexCount, GL_UNSIGNED_INT, 0)
    }
}

