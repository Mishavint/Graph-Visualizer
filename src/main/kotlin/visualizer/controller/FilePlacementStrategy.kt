package visualizer.controller

import tornadofx.Controller
import tornadofx.doubleProperty
import visualizer.view.GraphView

class FilePlacementStrategy: Controller() {

    fun place(graphView: GraphView,
              vertexInfo: MutableMap<String, GraphIO.VertexInfo>
    ) {
        graphView.vertices().forEach {
            val curInfo = vertexInfo[it.key.element]!!
            it.value.position = curInfo.centerX to curInfo.centerY
            it.value.rebindRadiusProperty(doubleProperty(curInfo.radius))
            it.value.color = curInfo.color
        }
    }
}