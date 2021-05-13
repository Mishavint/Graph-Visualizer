package visualazer.controller

import tornadofx.Controller
import tornadofx.doubleProperty
import visualazer.view.GraphView

class FilePlacementStrategy: Controller() {

    fun place(graphView: GraphView,
              vertexInfo: MutableMap<String, Triple<Double, Double, Double>>
    ) {
        graphView.vertexes().forEach {
            it.value.position = vertexInfo[it.key.element]!!.first to vertexInfo[it.key.element]!!.second
            it.value.reBindRadiusProperty(doubleProperty(vertexInfo[it.key.element]!!.third))
        }
    }
}