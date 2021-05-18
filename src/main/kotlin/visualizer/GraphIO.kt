package visualizer

import javafx.scene.paint.Color
import org.apache.commons.csv.*
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.FX.Companion.log
import tornadofx.c
import visualizer.view.*
import visualizer.model.*
import java.nio.file.*
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase

class GraphIO {
    data class VertexInfo(
        val centerX: Double,
        val centerY: Double,
        val radius: Double,
        val color: Color
    )

    object Neo4jConnectionTicket {
        var uri = "bolt://3.86.244.161:7687"
        var username = "neo4j"
        var password = "eighths-career-policy"
    }

    object VerticesTable : IdTable<String>() {
        override val id = varchar("id", 256).entityId()

        val centerX = double("CenterX")
        val centerY = double("CenterY")
        val radius = double("Radius")
        val color = varchar("Color", 10)
    }

    object EdgesTable : Table() {
        val firstVertex = reference("firstVertex", VerticesTable)
        val secondVertex = reference("secondVertex", VerticesTable)
        val element = varchar("Element", 256)
    }

    fun writeToFile(graphView: GraphView, fileName: String) {
        log.info("Writing to file was started")
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
                    itView.color.toString(), "-", "-", "-", "-", "-"
                )
            }
        }
        csvPrinter.flush()
        csvPrinter.close()
        log.info("Writing to file was finished")
    }

    fun readFromFile(graphView: GraphView, fileName: String): MutableMap<String, VertexInfo> {
        log.info("Reading from file was started")
        val reader = Files.newBufferedReader(Paths.get(fileName))
        val csvReader = CSVParser(reader, CSVFormat.DEFAULT)

        val graph = UndirectedGraph()

        val vertexInfo = mutableMapOf<String, VertexInfo>()
        for (rec in csvReader) {
            val v = rec[0]
            val u = rec[4]
            val e = rec[8]

            val vx = rec[1].split(" ")[0].toDouble()
            val vy = rec[1].split(" ")[1].toDouble()
            val vRadius = rec[2].toDouble()
            val vColor = c(rec[3])
            graph.addVertex(v)
            vertexInfo[v] = VertexInfo(vx, vy, vRadius, vColor)

            if (u != "-" && e != "-") {
                val ux = rec[5].split(" ")[0].toDouble()
                val uy = rec[5].split(" ")[1].toDouble()
                val uRadius = rec[6].toDouble()
                val uColor = c(rec[7])
                graph.addVertex(u)
                vertexInfo[u] = VertexInfo(ux, uy, uRadius, uColor)
                graph.addEdge(v, u, e)
            }
        }


        graphView.updateGraph(graph)
        csvReader.close()
        log.info("Reading from file was finished")
        return vertexInfo
    }

    fun readGraphEdges(graphView: GraphView, fileName: String) {
        log.info("Reading edges from file was started")
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
        log.info("Reading edges from file was finished")
    }

    fun writeToSQLite(graphView: GraphView, fileName: String) {
        log.info("Writing graph to data base was started (SQLLite)")
        if (fileName.isEmpty())
            return

        Database.connect("jdbc:sqlite:$fileName", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.drop(VerticesTable, EdgesTable)
            SchemaUtils.create(VerticesTable, EdgesTable)

            graphView.vertices().values.forEach { vertex ->
                VerticesTable.insert {
                    it[id] = vertex.vertex.element
                    it[centerX] = vertex.position.first
                    it[centerY] = vertex.position.second
                    it[radius] = vertex.radius
                    it[color] = vertex.color.toString()
                }
            }

            graphView.edges().forEach { edge ->
                EdgesTable.insert {
                    it[firstVertex] = edge.key.vertices.first.element
                    it[secondVertex] = edge.key.vertices.second.element
                    it[element] = edge.key.element
                }
            }
        }
        log.info("Writing graph to data base was finished (SQLLite)")
    }

    fun readFromSQLite(graphView: GraphView, fileName: String): MutableMap<String, VertexInfo> {
        log.info("Reading graph from data base was started (SQLLite)")
        Database.connect("jdbc:sqlite:${fileName}", "org.sqlite.JDBC")
        val vertexInfo = mutableMapOf<String, VertexInfo>()
        val graph = UndirectedGraph()

        transaction {
            VerticesTable.selectAll().forEach { vertexInTable ->
                val v = vertexInTable[VerticesTable.id].value
                val vx = vertexInTable[VerticesTable.centerX]
                val vy = vertexInTable[VerticesTable.centerY]
                val vRadius = vertexInTable[VerticesTable.radius]
                val vColor = c(vertexInTable[VerticesTable.color])

                graph.addVertex(v)
                vertexInfo[v] = VertexInfo(vx, vy, vRadius, vColor)
            }
            EdgesTable.selectAll().forEach { edgeInTable ->
                val v1 = edgeInTable[EdgesTable.firstVertex].toString()
                val v2 = edgeInTable[EdgesTable.secondVertex].toString()
                val vElement = edgeInTable[EdgesTable.element]

                graph.addEdge(v1, v2, vElement)
            }
        }

        graphView.updateGraph(graph)
        log.info("Reading graph from data base was finished (SQLLite)")
        return vertexInfo
    }

    fun writeToNeo4j(graphView: GraphView) {
        log.info("Writing graph to data base was started (Neo4j)")
        val driver = GraphDatabase.driver(Neo4jConnectionTicket.uri, AuthTokens.basic(Neo4jConnectionTicket.username, Neo4jConnectionTicket.password))
        val session = driver.session()

        session.writeTransaction {
            for (node in graphView.vertices().values) {
                it.run(
                    "CREATE(:vertices{id:\$id, centerX:\$centerX, centerY:\$centerY, radius:\$radius, color:\$color})",
                    mutableMapOf(
                        "id" to node.vertex.element,
                        "centerX" to node.position.first,
                        "centerY" to node.position.second,
                        "radius" to node.radius,
                        "color" to node.color.toString()
                    ) as Map<String, Any?>?
                )
            }

            for (edge in graphView.edges()) {
                it.run(
                    "MATCH(first:vertices{id:\$first_id}), (second:vertices{id:\$second_id}) MERGE(first)-[:edge{element:\$element}]-(second)",
                    mutableMapOf(
                        "first_id" to edge.key.vertices.first.element,
                        "second_id" to edge.key.vertices.second.element,
                        "element" to edge.key.element
                    ) as Map<String, Any?>?
                )
            }
        }

        session.close()
        driver.close()
        log.info("Writing graph to data base was finished (Neo4j)")
    }

    fun readFromNeo4j(graphView: GraphView): MutableMap<String, VertexInfo> {
        log.info("Reading graph from data base was started (Neo4j)")
        val vertexInfo = mutableMapOf<String, VertexInfo>()
        val graph = UndirectedGraph()

        val driver = GraphDatabase.driver(Neo4jConnectionTicket.uri, AuthTokens.basic(Neo4jConnectionTicket.username, Neo4jConnectionTicket.password))
        val session = driver.session()

        session.readTransaction {
            val vertexInfoFromNeo = it.run("MATCH(v:vertices) RETURN v.id AS id, v.centerX AS centerX, v.centerY AS centerY, v.radius AS radius, v.color AS color").list()
            val edgeInfoFromNeo = it.run("MATCH(first)-[edge]-(second) RETURN first.id AS first, second.id AS second, edge.element AS element").list()

            for (node in vertexInfoFromNeo) {
                val v = node["id"].toString()
                val vx = node["centerX"].asDouble()
                val vy = node["centerY"].asDouble()
                val vRadius = node["radius"].asDouble()
                val vColor = c(node["color"].asString())

                graph.addVertex(v)
                vertexInfo[v] = VertexInfo(vx, vy, vRadius, vColor)
            }

            for (edge in  edgeInfoFromNeo) {
                val v1 = edge["first"].toString()
                val v2 = edge["second"].toString()
                val vElement = edge["element"].toString()

                graph.addEdge(v1, v2, vElement)
            }
        }

        session.close()
        driver.close()

        graphView.updateGraph(graph)
        log.info("Reading graph from data base was finished (Neo4j)")
        return vertexInfo
    }
}