package visualizer.controller

import tornadofx.Controller
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import visualizer.view.VertexView
import java.lang.Double.min
import java.lang.Math.random
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

class CircularPlacementStrategy: Controller() {
    fun place(width: Double, height: Double, vertices: Collection<VertexView>) {
        log.info("Circular placement started")
        if (vertices.isEmpty()) {
            println("CircularPlacementStrategy.place: there is nothing to place 👐🏻")
            return
        }

        val center = Point2D(width / 2, height / 2)
        val angle = -360.0 / vertices.size

        val sorted = vertices.sortedBy { it.vertex.element }
        val first = sorted.first()
        var point = Point2D(center.x, center.y - min(width, height) / 2 + first.radius * 2)
        first.position = point.x to point.y
        first.color = Color.GRAY

        sorted
            .drop(1)
            .onEach {
                point = point.rotate(center, angle)
                it.position = point.x to point.y
                it.color = if (Random.nextBoolean()) Color.BLUE else Color.RED
            }
        log.info("Circular placement finished")
    }

    private fun Point2D.rotate(pivot: Point2D, degrees: Double): Point2D {
        val angle = Math.toRadians(degrees)
        val sin = sin(angle)
        val cos = cos(angle)

        val diff = subtract(pivot)
        val rotated = Point2D(
            diff.x * cos - diff.y * sin,
            diff.x * sin + diff.y * cos,
        )
        return rotated.add(pivot)
    }
}