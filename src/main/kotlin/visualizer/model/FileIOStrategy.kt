package visualizer.model

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import tornadofx.FX
import tornadofx.c
import visualizer.view.GraphView
import java.nio.file.Files
import java.nio.file.Paths

class FileIOStrategy: GraphIOStrategy {
    private val gap = "-"

    inner class CSVInfo(rec: CSVRecord) {
        val label1: String = rec[0]
        val position1: String = rec[1]
        val radius1: String = rec[2]
        val color1: String = rec[3]
        val label2: String = rec[4]
        val position2: String = rec[5]
        val radius2: String = rec[6]
        val color2: String = rec[7]
        val edgeLabel: String = rec[8]
    }

    override fun read(graphView: GraphView, fileName: String): MutableMap<String, GraphIOStrategy.VertexInfo> {
        FX.log.info("Reading from file was started")
        val reader = Files.newBufferedReader(Paths.get(fileName))
        val csvReader = CSVParser(reader, CSVFormat.DEFAULT)

        val graph = UndirectedGraph()

        val vertexInfo = mutableMapOf<String, GraphIOStrategy.VertexInfo>()
        for (rec in csvReader) {
            val info = CSVInfo(rec)
            val v = info.label1
            val u = info.label2
            val e = info.edgeLabel

            val vx = info.position1.split(" ")[0].toDouble()
            val vy = info.position1.split(" ")[1].toDouble()
            val vRadius = info.radius1.toDouble()
            val vColor = c(info.color1)
            graph.addVertex(v)
            vertexInfo[v] = GraphIOStrategy.VertexInfo(vx, vy, vRadius, vColor)

            if (u != gap && e != gap) {
                val ux = info.position2.split(" ")[0].toDouble()
                val uy = info.position2.split(" ")[1].toDouble()
                val uRadius = info.radius2.toDouble()
                val uColor = c(info.color2)
                graph.addVertex(u)
                vertexInfo[u] = GraphIOStrategy.VertexInfo(ux, uy, uRadius, uColor)
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
                    itView.color.toString(), gap, gap, gap, gap, gap
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