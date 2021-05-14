package visualizer.controller

import javafx.scene.paint.Color
import tornadofx.Controller
import visualizer.view.VertexView
import java.util.*

class RandomPlacementStrategy: Controller() {

    private val random = Random()

    private fun rand(to: Int): Double {
        return (random.nextInt(to - 10) + 10).toDouble()
    }

    fun place(width: Double, height: Double, vertexes: Collection<VertexView>) {
        if (vertexes.isEmpty()) {
            println("There is no vertexes")
            return
        }

        val radius = vertexes.first().radius

        vertexes.forEach {
            it.position = rand(width.toInt() - radius.toInt() * 10) to rand(height.toInt() - radius.toInt() * 10)
            it.color = if (random.nextBoolean()) Color.RED else Color.BLUE
        }
    }
}