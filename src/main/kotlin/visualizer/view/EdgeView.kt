package visualizer.view

import javafx.scene.shape.Line
import visualizer.model.Edge

class EdgeView(
    edge: Edge,
    first: VertexView,
    second: VertexView,)
    : Line() {

    init {
        startXProperty().bind(first.centerXProperty())
        startYProperty().bind(first.centerYProperty())
        endXProperty().bind(second.centerXProperty())
        endYProperty().bind(second.centerYProperty())
    }
}