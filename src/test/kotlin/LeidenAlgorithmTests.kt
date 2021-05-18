import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertDoesNotThrow
import visualizer.view.*
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

class LeidenAlgorithmTests {
    private val graphView = GraphView()

    @BeforeEach
    fun setUp() {
        graphView.updateGraph(props.SAMPLE_GRAPH)
    }

    private val leiden = Algorithms(graphView)

    @Test
    fun `leiden Algorithm is working`() {
        assertDoesNotThrow {
            leiden.communitiesDetection(0.5)
        }
    }

    @Test
    fun `leiden Algorithm with 1 resolution will do as many communities as number of vertices`() {
        leiden.communitiesDetection(1.0)

            csvReader().open("tmp/fileAfterLeidenAlg.csv") {
                var line: List<String>? = readNext()
                while (line != null) {
                    val column = line[0]

                    val digits = column.split("\t")

                    assertEquals(digits[0], digits[1])

                    line = readNext()
                }
        }
    }
}