package visualazer.controller

import visualazer.view.VertexView
import javafx.scene.input.MouseEvent
import tornadofx.Controller

class VertexDragController: Controller() {
    fun dragVertex(event: MouseEvent) {
        val v = check(event)
        v.centerX = event.x
        v.centerY = event.y
        event.consume()
    }

    private fun check(event: MouseEvent): VertexView {
        require(event.target is VertexView)
        { "handler supposed to process events only for vertices: $event" }
        return event.target as VertexView
    }
}