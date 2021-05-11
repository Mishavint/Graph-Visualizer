package visualazer

import visualazer.view.*
import org.apache.commons.csv.*
import java.nio.file.Files
import java.nio.file.Paths

class GraphIO {
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
}