package visualazer.controller

import javafx.scene.paint.Color
import tornadofx.Controller
import visualazer.view.VertexView

class VertexController : Controller() {
    fun setBlackColor(vertices : Collection<VertexView>) {
        vertices.forEach {
            it.color = Color.BLACK
        }
    }
}