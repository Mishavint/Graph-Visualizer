package visualazer.view

import visualazer.model.*
import visualazer.controller.VertexDragController
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.add
import tornadofx.doubleProperty
import tornadofx.find

class GraphView(private var graph: Graph = UndirectedGraph()): Pane() {
    private val dragger = find(VertexDragController::class)
    private val vertexes by lazy {
        graph.vertexes().associateWith {
            VertexView(it, 0.0, 0.0, doubleProperty(5.0), Color.BLACK)
        }.toMutableMap()
    }

    private val edges by lazy {
        graph.edges().associateWith {
            val first = vertexes[it.vertexes.first]
                ?: throw IllegalStateException("VertexView for ${it.vertexes.first} not found")
            val second = vertexes[it.vertexes.second]
                ?: throw IllegalStateException("VertexView for ${it.vertexes.second} not found")
            EdgeView(it, first, second)
        }.toMutableMap()
    }

    fun graph() = graph
    fun vertexes(): Map<Vertex, VertexView> = vertexes
    fun edges(): Map<Edge, EdgeView> = edges

    fun setGraph(graph: Graph) {
        this.graph = graph
    }

    init {
        vertexes().forEach { v ->
            add(v.value)
            v.value.setOnMouseDragged { e -> dragger.dragVertex(e) }
        }
        edges().forEach {
            add(it.value)
        }
    }

    fun clearGraph() {
        graph.edges().clear()
        graph.vertexes().clear()
        this.vertexes.clear()
        this.edges.clear()
    }
}