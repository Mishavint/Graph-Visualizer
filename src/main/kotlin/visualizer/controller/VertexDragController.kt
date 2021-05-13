package visualizer.controller

import visualizer.view.VertexView
import javafx.scene.input.MouseEvent
import tornadofx.Controller

class VertexDragController: Controller() {
    fun dragVertex(event: MouseEvent) {
        val v = check(event)
        v.centerX = validPosition(event.x, 0.0, v.parent.layoutBounds.width, v.radius)
        v.centerY = validPosition(event.y, 0.0, v.parent.layoutBounds.height, v.radius)
        event.consume()
    }

    private fun check(event: MouseEvent): VertexView {
        require(event.target is VertexView)
        { "handler supposed to process events only for vertices: $event" }
        return event.target as VertexView
    }

    private fun validPosition(value: Double, min: Double, max: Double, padding: Double) = when {
        value < min + padding -> min + padding
        value > max - padding -> max - padding
        else -> value
    }
}