package visualizer.model

import javafx.scene.paint.Color
import visualizer.view.GraphView

interface GraphIO {
    data class VertexInfo(
        val centerX: Double,
        val centerY: Double,
        val radius: Double,
        val color: Color
    )

    fun write(graphView: GraphView, fileName: String)

    fun read(graphView: GraphView, fileName: String): MutableMap<String, VertexInfo>
}