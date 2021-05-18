package visualizer.view

import visualizer.model.*
import visualizer.controller.VertexDragController
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*

class GraphView(private var graph: Graph = UndirectedGraph()): Pane() {
    private val dragger = find(VertexDragController::class)
    private var vertices = graph.vertices().associateWith {
        VertexView(it, 0.0, 0.0, doubleProperty(5.0), Color.BLACK)
    }.toMutableMap()


    private var edges = addEdges()

    fun graph() = graph
    fun vertices(): Map<Vertex, VertexView> = vertices
    fun edges(): Map<Edge, EdgeView> = edges

    init {
        edges().forEach {
            add(it.value)
        }

        vertices().forEach { v ->
            add(v.value)
            v.value.setOnMouseDragged { e -> dragger.dragVertex(e) }
        }
    }

    fun updateGraph(graph: Graph) {
        this.clear()
        this.graph = graph
        vertices = graph.vertices().associateWith {
            VertexView(it, 0.0, 0.0, doubleProperty(5.0), Color.BLACK)
        }.toMutableMap()

        edges = addEdges()


        edges().forEach {
            add(it.value)
        }

        vertices().forEach { v ->
            add(v.value)
            v.value.setOnMouseDragged { e -> dragger.dragVertex(e) }
        }
    }

    private fun addEdges() = graph.edges().associateWith {
        val first = vertices[it.vertices.first]
            ?: throw IllegalStateException("VertexView for ${it.vertices.first} not found")
        val second = vertices[it.vertices.second]
            ?: throw IllegalStateException("VertexView for ${it.vertices.second} not found")
        EdgeView(it, first, second)
    }.toMutableMap()
}