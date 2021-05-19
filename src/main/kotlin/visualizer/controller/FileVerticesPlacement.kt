package visualizer.controller

import tornadofx.Controller
import tornadofx.doubleProperty
import visualizer.model.GraphIOStrategy
import visualizer.view.GraphView

class FileVerticesPlacement: Controller() {

    fun place(graphView: GraphView,
              vertexInfo: MutableMap<String, GraphIOStrategy.VertexInfo>
    ) {
        graphView.vertices().forEach {
            val curInfo = vertexInfo[it.key.element]!!
            it.value.position = curInfo.centerX to curInfo.centerY
            it.value.rebindRadiusProperty(doubleProperty(curInfo.radius))
            it.value.color = curInfo.color
        }
    }
}