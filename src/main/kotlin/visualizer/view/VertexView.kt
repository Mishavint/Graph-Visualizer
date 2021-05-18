package visualizer.view

import visualizer.model.Vertex
import javafx.scene.shape.Circle
import javafx.beans.property.DoubleProperty
import javafx.scene.paint.Color

class VertexView(
    val vertex: Vertex,
    x: Double,
    y: Double,
    r: DoubleProperty,
    color: Color
) : Circle(x, y, r.get(), color){

    init {
        radiusProperty().bind(r)
    }

    var position: Pair<Double, Double>
        get() = centerX to centerY
        set(value) {
            centerX = value.first
            centerY = value.second
        }

    var color: Color
        get() = fill as Color
        set(value) {
            fill = value
        }

    fun rebindRadiusProperty(radius: DoubleProperty) {
        radiusProperty().bind(radius)
    }
}