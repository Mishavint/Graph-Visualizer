import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import visualizer.model.UndirectedGraph
import visualizer.view.GraphView
import visualizer.controller.GraphIO
import java.nio.file.Files
import java.nio.file.Paths

class FileTest {

    private val graph = UndirectedGraph().apply {
        addVertex("1")
        addVertex("2")
        addVertex("3")
        addVertex("4")

        addEdge("1", "2", "12")
        addEdge("1", "3", "13")
        addEdge("2", "3", "23")
    }

    @Nested
    inner class Save {
        private val fileName = "src/test/kotlin/saveTest.csv"
        private val graphView = GraphView(graph)

        @Test
        fun `Saving works as expected`() {
            GraphIO().writeToFile(graphView, fileName)

            val expectedNodesInFile = ArrayList<String>()
            expectedNodesInFile.add("1,0.0 0.0,5.0,0x000000ff,2,0.0 0.0,5.0,0x000000ff,12")
            expectedNodesInFile.add("2,0.0 0.0,5.0,0x000000ff,3,0.0 0.0,5.0,0x000000ff,23")
            expectedNodesInFile.add("1,0.0 0.0,5.0,0x000000ff,3,0.0 0.0,5.0,0x000000ff,13")
            expectedNodesInFile.add("4,0.0 0.0,5.0,0x000000ff,-,-,-,-,-")

            val reader = Files.newBufferedReader(Paths.get(fileName))

            val lines = reader.readLines()

            for (i in lines.indices) {
                assertEquals(lines[i], expectedNodesInFile[i])
            }
        }

    }

    @Nested
    inner class Reading {
        private val fileName = "src/test/kotlin/readingTest.csv"

        private val graphView = GraphView()

        val verticesInfo = GraphIO().readFromFile(graphView, fileName)

        @Test
        fun `reading works as expected`() {
            val labels = arrayListOf("1", "2", "3", "4", "5")
            var index = 0
            for (vertex in graphView.vertices().keys) {
                assertEquals(vertex.element, labels[index++])
            }
        }
    }
}