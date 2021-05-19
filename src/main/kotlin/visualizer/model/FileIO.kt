package visualizer.model

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import tornadofx.FX
import tornadofx.c
import visualizer.view.GraphView
import java.nio.file.Files
import java.nio.file.Paths

const val LABEL1_INDEX = 0
const val POSITION1_INDEX = 1
const val RADIUS1_INDEX = 2
const val COLOR1_INDEX = 3
const val LABEL2_INDEX = 4
const val POSITION2_INDEX = 5
const val RADIUS2_INDEX = 6
const val COLOR2_INDEX = 7
const val EDGE_INDEX = 8
const val GAP_SYM = "-"

class FileIO: GraphIO {

    override fun read(graphView: GraphView, fileName: String): MutableMap<String, GraphIO.VertexInfo> {
        FX.log.info("Reading from file was started")
        val reader = Files.newBufferedReader(Paths.get(fileName))
        val csvReader = CSVParser(reader, CSVFormat.DEFAULT)

        val graph = UndirectedGraph()

        val vertexInfo = mutableMapOf<String, GraphIO.VertexInfo>()
        for (rec in csvReader) {
            val v = rec[LABEL1_INDEX]
            val u = rec[LABEL2_INDEX]
            val e = rec[EDGE_INDEX]

            val vx = rec[POSITION1_INDEX].split(" ")[0].toDouble()
            val vy = rec[POSITION1_INDEX].split(" ")[1].toDouble()
            val vRadius = rec[RADIUS1_INDEX].toDouble()
            val vColor = c(rec[COLOR1_INDEX])
            graph.addVertex(v)
            vertexInfo[v] = GraphIO.VertexInfo(vx, vy, vRadius, vColor)

            if (u != GAP_SYM && e != GAP_SYM) {
                val ux = rec[POSITION2_INDEX].split(" ")[0].toDouble()
                val uy = rec[POSITION2_INDEX].split(" ")[1].toDouble()
                val uRadius = rec[RADIUS2_INDEX].toDouble()
                val uColor = c(rec[COLOR2_INDEX])
                graph.addVertex(u)
                vertexInfo[u] = GraphIO.VertexInfo(ux, uy, uRadius, uColor)
                graph.addEdge(v, u, e)
            }
        }


        graphView.updateGraph(graph)
        csvReader.close()
        FX.log.info("Reading from file was finished")
        return vertexInfo
    }

    override fun write(graphView: GraphView, fileName: String) {
        FX.log.info("Writing to file was started")
        val visited = graphView.vertices().keys.associateWith {
            false
        }.toMutableMap()
        val writer = Files.newBufferedWriter(Paths.get(fileName))
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

        graphView.edges().forEach {
            val v = it.key.vertices.first
            val u = it.key.vertices.second
            val vView = graphView.vertices()[v]
                ?: throw IllegalStateException("VertexView for $v not found")
            val uView = graphView.vertices()[u]
                ?: throw IllegalStateException("VertexView for $u not found")
            csvPrinter.printRecord(
                v.element, "${vView.centerX} ${vView.centerY}", vView.radius, vView.color.toString(),
                u.element, "${uView.centerX} ${uView.centerY}", uView.radius, uView.color.toString(), it.key.element
            )
            visited[v] = true
            visited[u] = true
        }
        graphView.vertices().forEach {
            if (!visited[it.key]!!) {
                val itView = graphView.vertices()[it.key]
                    ?: throw IllegalStateException("VertexView for $it not found")
                csvPrinter.printRecord(
                    it.key.element, "${itView.centerX} ${itView.centerY}", itView.radius,
                    itView.color.toString(), GAP_SYM, GAP_SYM, GAP_SYM, GAP_SYM, GAP_SYM
                )
            }
        }
        csvPrinter.flush()
        csvPrinter.close()
        FX.log.info("Writing to file was finished")
    }

    fun readGraphEdges(graphView: GraphView, fileName: String) {
        FX.log.info("Reading edges from file was started")
        val reader = Files.newBufferedReader(Paths.get(fileName))

        val lines = reader.readLines()

        val graph = UndirectedGraph()

        for (i in 1 until lines.size) {
            val edge = lines[i].split(",", " ")
            graph.addVertex(edge[0])
            graph.addVertex(edge[1])
            graph.addEdge(edge[0], edge[1], "${i * 4000}")
        }
        graphView.updateGraph(graph)
        FX.log.info("Reading edges from file was finished")
    }
}