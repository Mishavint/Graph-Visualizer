package visualizer.controller

import javafx.scene.paint.Color
import tornadofx.Controller
import visualizer.view.VertexView

class VertexController : Controller() {
    fun setBlackColor(vertices : Collection<VertexView>) {
        vertices.forEach {
            it.color = Color.BLACK
        }
    }
}