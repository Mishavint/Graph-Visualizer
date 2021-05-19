import javafx.beans.property.SimpleDoubleProperty
import org.junit.jupiter.api.*
import visualizer.model.GraphIO
import visualizer.controller.FilePlacementStrategy
import visualizer.view.*
import java.io.File
import org.junit.jupiter.api.Assertions.*
import visualizer.model.Neo4jIO
import visualizer.model.SQLiteIO

class SqliteTest {
    private val graph = GraphView(props.SAMPLE_GRAPH)
    private val graphIO = SQLiteIO()
    private val fileNameForSqliteTest = "src/test/kotlin/SQLiteTest"

    @Test
    fun `Write to SQLite`() {
        assertDoesNotThrow {
           graphIO.write(graph, fileNameForSqliteTest)
        }
        File(fileNameForSqliteTest).delete()
    }

    @Test
    fun `Read from SQLite`() {
        graphIO.write(graph, fileNameForSqliteTest)
        assertDoesNotThrow {
            val vertexInfo = graphIO.read(graph, fileNameForSqliteTest)
            FilePlacementStrategy().place(graph, vertexInfo)
        }
        File(fileNameForSqliteTest).delete()
    }

    @Test
    fun `SQLIte contains graph`() {
        graphIO.write(graph, fileNameForSqliteTest)

        graph.vertices().values.forEach {
            it.rebindRadiusProperty(SimpleDoubleProperty(it.radius + 1.0))
        }
        val oldVertices = graph.vertices().values.toTypedArray()

        val vertexInfo = graphIO.read(graph, fileNameForSqliteTest)
        FilePlacementStrategy().place(graph, vertexInfo)
        val newVertices = graph.vertices().values.toTypedArray()

        for(i in oldVertices.indices) {
            assertNotEquals( oldVertices[i], newVertices[i] )
        }

        File(fileNameForSqliteTest).delete()
    }
}