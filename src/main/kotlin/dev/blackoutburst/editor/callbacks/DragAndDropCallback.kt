package dev.blackoutburst.editor.callbacks

import dev.blackoutburst.editor.Main
import dev.blackoutburst.editor.Main.Companion.queue
import dev.blackoutburst.editor.files.FileManager
import dev.blackoutburst.editor.tiles.TilesManager
import dev.blackoutburst.editor.ui.TilePanel
import org.lwjgl.glfw.GLFWDropCallback
import org.lwjgl.glfw.GLFWDropCallbackI
import java.io.File

class DragAndDropCallback : GLFWDropCallbackI {
    override fun invoke(window: Long, count: Int, names: Long) {
        for (i in 0 until count) {
            val path = GLFWDropCallback.getName(names, i)
            if (path.endsWith(".png", true)) {
                val file = File(path)
                val dest = File("${Main.textureFolder}/${file.name}")
                file.copyTo(dest, true)

                TilePanel.refreshPanel()
                TilesManager.refreshDiffuseMap()
            }
            if (path.endsWith(".2de", true)) {
                queue.add {
                    FileManager.load(path)
                    TilesManager.refreshDiffuseMap()
                }
            }
        }
    }
}
