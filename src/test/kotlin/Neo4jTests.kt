import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import visualizer.GraphIO
import visualizer.view.GraphView

internal class Neo4jTests {
    @Nested
    inner class Connection {
        @Test
        fun `validating uri, username, password`() {
            val driver = GraphDatabase.driver(
                GraphIO.Neo4jConnectionTicket.uri,
                AuthTokens.basic(GraphIO.Neo4jConnectionTicket.username, GraphIO.Neo4jConnectionTicket.password)
            )
            val session = driver.session()

            session.beginTransaction()

            session.close()
            driver.close()
        }
    }

    @Nested
    inner class DBmanage {
        private val testData = Triple("bolt://3.239.150.207:7687", "neo4j", "wraps-span-canal")
        private val userData = Triple(
            GraphIO.Neo4jConnectionTicket.uri,
            GraphIO.Neo4jConnectionTicket.username,
            GraphIO.Neo4jConnectionTicket.password
        )

        private var tmpGraph = GraphView()
        private val falseGraph = GraphView()

        init {
            GraphIO().readGraphEdges(falseGraph, "graphs/soc-karate.mtx")
        }

        private fun changeDBConnectionData(tr: Triple<String, String, String>) {
            GraphIO.Neo4jConnectionTicket.uri = tr.first
            GraphIO.Neo4jConnectionTicket.username = tr.second
            GraphIO.Neo4jConnectionTicket.password = tr.third
        }

        private fun cleanDB() {
            val driver = GraphDatabase.driver(
                GraphIO.Neo4jConnectionTicket.uri,
                AuthTokens.basic(GraphIO.Neo4jConnectionTicket.username, GraphIO.Neo4jConnectionTicket.password)
            )
            val session = driver.session()

            session.writeTransaction {
                it.run("MATCH (n) DETACH DELETE n;")
            }

            session.close()
            driver.close()
        }

        private fun compareGraphs(g1: GraphView, g2: GraphView): Boolean {
            val l1 = mutableListOf<Pair<String, Triple<Double, Double, javafx.scene.paint.Paint>>>()
            val l2 = mutableListOf<Pair<String, Triple<Double, Double, javafx.scene.paint.Paint>>>()

            for (v in g1.vertices()) {
                l1.add(Pair(v.key.element, Triple(v.value.centerX, v.value.centerY, v.value.fill)))
            }

            for (v in g2.vertices()) {
                l2.add(Pair(v.key.element, Triple(v.value.centerX, v.value.centerY, v.value.fill)))
            }

            l1.sortBy { it.first }
            l2.sortBy { it.first }

            if (l1.size != l2.size) return false
            for (i in 0 until l1.size) {
                if (l1[i].second.first != l2[i].second.first) return false
                if (l1[i].second.second != l2[i].second.second) return false
                if (l1[i].second.third != l2[i].second.third) return false
            }

            return true
        }

        @Test
        fun `Write to Neo DB`() {
            changeDBConnectionData(testData)

            cleanDB()

            GraphIO().writeToNeo4j(falseGraph)
            changeDBConnectionData(userData)
        }

        @Test
        fun `Read from Neo DB`() {
            changeDBConnectionData(testData)
            GraphIO().readFromNeo4j(tmpGraph)

            assertTrue(compareGraphs(tmpGraph, falseGraph))

            changeDBConnectionData(userData)
        }
    }
}