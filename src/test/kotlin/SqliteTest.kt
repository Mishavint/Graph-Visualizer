import javafx.beans.property.SimpleDoubleProperty
import org.junit.jupiter.api.*
import visualizer.GraphIO
import visualizer.controller.FilePlacementStrategy
import visualizer.view.*
import java.io.File
import org.junit.jupiter.api.Assertions.*

class SqliteTest {
    private val graph = GraphView(props.SAMPLE_GRAPH)
    private val graphIO = GraphIO()
    private val fileNameForSqliteTest = "src/test/kotlin/SQLiteTest"

    @Test
    fun `Write to SQLite`() {
        assertDoesNotThrow {
           graphIO.writeToSQLite(graph, fileNameForSqliteTest)
        }
        File(fileNameForSqliteTest).delete()
    }

    @Test
    fun `Read from SQLite`() {
        graphIO.writeToSQLite(graph, fileNameForSqliteTest)
        assertDoesNotThrow {
            val vertexInfo = graphIO.readFromSQLite(graph, fileNameForSqliteTest)
            FilePlacementStrategy().place(graph, vertexInfo)
        }
        File(fileNameForSqliteTest).delete()
    }

    @Test
    fun `SQLIte contains graph`() {
        graphIO.writeToSQLite(graph, fileNameForSqliteTest)

        graph.vertices().values.forEach {
            it.rebindRadiusProperty(SimpleDoubleProperty(it.radius + 1.0))
        }
        val oldVertices = graph.vertices().values.toTypedArray()

        val vertexInfo = graphIO.readFromSQLite(graph, fileNameForSqliteTest)
        FilePlacementStrategy().place(graph, vertexInfo)
        val newVertices = graph.vertices().values.toTypedArray()

        for(i in oldVertices.indices) {
            assertNotEquals( oldVertices[i], newVertices[i] )
        }

        File(fileNameForSqliteTest).delete()
    }
}