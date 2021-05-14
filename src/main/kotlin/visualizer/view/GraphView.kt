package visualizer.view

import visualizer.model.*
import visualizer.controller.VertexDragController
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*

class GraphView(private var graph: Graph = UndirectedGraph()): Pane() {
    private val dragger = find(VertexDragController::class)
    private var vertexes = graph.vertexes().associateWith {
        VertexView(it, 0.0, 0.0, doubleProperty(5.0), Color.BLACK)
    }.toMutableMap()


    private var edges = addEdges()

    fun graph() = graph
    fun vertexes(): Map<Vertex, VertexView> = vertexes
    fun edges(): Map<Edge, EdgeView> = edges

    init {
        edges().forEach {
            add(it.value)
        }

        vertexes().forEach { v ->
            add(v.value)
            v.value.setOnMouseDragged { e -> dragger.dragVertex(e) }
        }
    }

    fun updateGraph(graph: Graph) {
        this.clear()
        this.graph = graph
        vertexes = graph.vertexes().associateWith {
            VertexView(it, 0.0, 0.0, doubleProperty(5.0), Color.BLACK)
        }.toMutableMap()

        edges = addEdges()


        edges().forEach {
            add(it.value)
        }

        vertexes().forEach { v ->
            add(v.value)
            v.value.setOnMouseDragged { e -> dragger.dragVertex(e) }
        }
    }

    private fun addEdges() = graph.edges().associateWith {
        val first = vertexes[it.vertexes.first]
            ?: throw IllegalStateException("VertexView for ${it.vertexes.first} not found")
        val second = vertexes[it.vertexes.second]
            ?: throw IllegalStateException("VertexView for ${it.vertexes.second} not found")
        EdgeView(it, first, second)
    }.toMutableMap()
}