package visualizer


import javafx.scene.paint.Color
import org.apache.commons.csv.*
import tornadofx.c
import visualizer.view.*
import visualizer.model.UndirectedGraph
import java.nio.file.Files
import java.nio.file.Paths

class GraphIO {
    data class VertexInfo(val centerX: Double,
                     val centerY: Double,
                     val radius: Double,
                     val color: Color)

    fun writeToFile(graphView: GraphView, fileName: String) {
        val visited = graphView.vertexes().keys.associateWith {
            false
        }.toMutableMap()
        val writer = Files.newBufferedWriter(Paths.get(fileName))
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

        graphView.edges().forEach {
            val v = it.key.vertexes.first
            val u = it.key.vertexes.second
            val vView = graphView.vertexes()[v]
                ?: throw IllegalStateException("VertexView for $v not found")
            val uView = graphView.vertexes()[u]
                ?: throw IllegalStateException("VertexView for $u not found")
            csvPrinter.printRecord(v.element, "${vView.centerX} ${vView.centerY}", vView.radius, vView.color.toString(),
                u.element, "${uView.centerX} ${uView.centerY}", uView.radius, uView.color.toString(), it.key.element)
            visited[v] = true
            visited[u] = true
        }
        graphView.vertexes().forEach {
            if (!visited[it.key]!!) {
                val itView = graphView.vertexes()[it.key]
                    ?: throw IllegalStateException("VertexView for $it not found")
                csvPrinter.printRecord(it.key.element, "${itView.centerX} ${itView.centerY}", itView.radius,
                    itView.color.toString(), "-", "-", "-", "-", "-")
            }
        }
        csvPrinter.flush()
        csvPrinter.close()
    }

    fun readFromFile(graphView: GraphView, fileName: String): MutableMap<String, VertexInfo> {
        val reader = Files.newBufferedReader(Paths.get(fileName))
        val csvReader = CSVParser(reader, CSVFormat.DEFAULT)

        val graph = UndirectedGraph()

        val vertexInfo = mutableMapOf<String, VertexInfo>()
        for (rec in csvReader) {
            val v = rec[0]
            val u = rec[4]
            val e = rec[8]

            val vx = rec[1].split(" ")[0].toDouble()
            val vy = rec[1].split(" ")[1].toDouble()
            val vRadius = rec[2].toDouble()
            val vColor = c(rec[3])
            graph.addVertex(v)
            vertexInfo[v] = VertexInfo(vx, vy, vRadius, vColor)

            if (u != "-" && e != "-") {
                val ux = rec[5].split(" ")[0].toDouble()
                val uy = rec[5].split(" ")[1].toDouble()
                val uRadius = rec[6].toDouble()
                val uColor = c(rec[7])
                graph.addVertex(u)
                vertexInfo[u] = VertexInfo(ux, uy, uRadius, uColor)
                graph.addEdge(v, u, e)
            }
        }
        graphView.updateGraph(graph)
        csvReader.close()
        return vertexInfo
    }

    fun readGraphEdges(graphView: GraphView, fileName: String) {
        val reader = Files.newBufferedReader(Paths.get(fileName))

        val lines = reader.readLines()

        val graph = UndirectedGraph()

        for (i in 1 until lines.size) {
            val edge = lines[i].split(",", " ")
            graph.addVertex(edge[0])
            graph.addVertex(edge[1])
            graph.addEdge(edge[0], edge[1], "${ i * 4000 }")
        }
        graphView.updateGraph(graph)
    }
}