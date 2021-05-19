import javafx.beans.property.SimpleDoubleProperty
import org.junit.jupiter.api.*
import visualizer.controller.FileVerticesPlacement
import visualizer.view.*
import java.io.File
import org.junit.jupiter.api.Assertions.*
import visualizer.model.SQLiteIOStrategy

class SqliteTest {
    private val graph = GraphView(props.SAMPLE_GRAPH)
    private val graphIO = SQLiteIOStrategy()
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
            FileVerticesPlacement().place(graph, vertexInfo)
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
        FileVerticesPlacement().place(graph, vertexInfo)
        val newVertices = graph.vertices().values.toTypedArray()

        for(i in oldVertices.indices) {
            assertNotEquals( oldVertices[i], newVertices[i] )
        }

        File(fileNameForSqliteTest).delete()
    }
}