package visualizer.controller

import tornadofx.Controller
import tornadofx.doubleProperty
import visualizer.GraphIO
import visualizer.view.GraphView

class FilePlacementStrategy: Controller() {

    fun place(graphView: GraphView,
              vertexInfo: MutableMap<String, GraphIO.VertexInfo>
    ) {
        graphView.vertexes().forEach {
            val curInfo = vertexInfo[it.key.element]!!
            it.value.position = curInfo.centerX to curInfo.centerY
            it.value.reBindRadiusProperty(doubleProperty(curInfo.radius))
            it.value.color = curInfo.color
        }
    }
}