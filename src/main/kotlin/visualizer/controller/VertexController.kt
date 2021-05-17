package visualizer.controller

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.paint.Color
import tornadofx.Controller
import visualizer.view.VertexView

class VertexController : Controller() {
    fun setBlackColor(vertices : Collection<VertexView>) {
        vertices.forEach {
            it.color = Color.BLACK
        }
    }

    fun increaseRadius(vertices : Collection<VertexView>) {
        vertices.forEach {
            val newRadius = SimpleDoubleProperty(it.radius * 1.05 )
            it.reBindRadiusProperty(newRadius)
        }
    }

    fun decreaseRadius(vertices: Collection<VertexView>) {
        vertices.forEach {
            val newRadius = SimpleDoubleProperty(it.radius / 1.05 )
            it.reBindRadiusProperty(newRadius)
        }
    }
}