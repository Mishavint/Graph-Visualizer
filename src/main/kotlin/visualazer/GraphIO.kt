package visualazer

import visualazer.view.*
import org.apache.commons.csv.*
import visualazer.model.UndirectedGraph
import java.nio.file.Files
import java.nio.file.Paths

class GraphIO {

    fun writeToFile(graphView: GraphView, fileName: String) {
        val visited = graphView.vertexes().keys.associateWith {
            false
        }.toMutableMap()
        val writer = Files.newBufferedWriter(Paths.get("graphs/$fileName"))
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
        graphView.edges().forEach {
            val v = it.key.vertexes.first
            val u = it.key.vertexes.second
            val vView = graphView.vertexes()[v]
                ?: throw IllegalStateException("VertexView for $v not found")
            val uView = graphView.vertexes()[u]
                ?: throw IllegalStateException("VertexView for $u not found")
            csvPrinter.printRecord(v.element, "${vView.centerX} ${vView.centerY}", vView.radius,
                u.element, "${uView.centerX} ${uView.centerY}", uView.radius, it.key.element)
            visited[v] = true
            visited[u] = true
        }
        graphView.vertexes().forEach {
            if (!visited[it.key]!!) {
                val itView = graphView.vertexes()[it.key]
                    ?: throw IllegalStateException("VertexView for $it not found")
                csvPrinter.printRecord(it.key.element, "${itView.centerX} ${itView.centerY}", itView.radius,
                    "-", "-", "-","-")
            }
        }
        csvPrinter.flush()
        csvPrinter.close()
    }

    fun readFromFile(graphView: GraphView, fileName: String): MutableMap<String, Triple<Double, Double, Double>> {
        val reader = Files.newBufferedReader(Paths.get(fileName))
        val csvReader = CSVParser(reader, CSVFormat.DEFAULT)

        val graph = UndirectedGraph()

        val vertexInfo = mutableMapOf<String, Triple<Double, Double, Double>>()
        for (rec in csvReader) {
            val v = rec[0]
            val u = rec[3]
            val e = rec[6]

            val vx = rec[1].split(" ")[0].toDouble()
            val vy = rec[1].split(" ")[1].toDouble()
            val vRadius = rec[2].toDouble()
            graph.addVertex(v)
            vertexInfo[v] = Triple(vx, vy, vRadius)

            if (u != "-" && e != "-") {
                val ux = rec[4].split(" ")[0].toDouble()
                val uy = rec[4].split(" ")[1].toDouble()
                val uRadius = rec[5].toDouble()
                graph.addVertex(u)
                vertexInfo[u] = Triple(ux, uy, uRadius)
                graph.addEdge(v, u, e)
            }
        }
        graphView.setGraph(graph)
        return vertexInfo
    }
}