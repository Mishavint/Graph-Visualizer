package visualizer.model

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.FX
import tornadofx.c
import visualizer.view.GraphView

class SQLiteIOStrategy: GraphIOStrategy {

    object VerticesTable : IdTable<String>() {
        override val id = VerticesTable.varchar("id", 256).entityId()

        val centerX = VerticesTable.double("CenterX")
        val centerY = VerticesTable.double("CenterY")
        val radius = VerticesTable.double("Radius")
        val color = VerticesTable.varchar("Color", 10)
    }

    object EdgesTable : Table() {
        val firstVertex = reference("firstVertex", VerticesTable)
        val secondVertex = reference("secondVertex", VerticesTable)
        val element = varchar("Element", 256)
    }

    override fun read(graphView: GraphView, fileName: String): MutableMap<String, GraphIOStrategy.VertexInfo> {
        FX.log.info("Reading graph from data base was started (SQLLite)")
        Database.connect("jdbc:sqlite:${fileName}", "org.sqlite.JDBC")
        val vertexInfo = mutableMapOf<String, GraphIOStrategy.VertexInfo>()
        val graph = UndirectedGraph()

        transaction {
            VerticesTable.selectAll().forEach { vertexInTable ->
                val v = vertexInTable[VerticesTable.id].value
                val vx = vertexInTable[VerticesTable.centerX]
                val vy = vertexInTable[VerticesTable.centerY]
                val vRadius = vertexInTable[VerticesTable.radius]
                val vColor = c(vertexInTable[VerticesTable.color])

                graph.addVertex(v)
                vertexInfo[v] = GraphIOStrategy.VertexInfo(vx, vy, vRadius, vColor)
            }
            EdgesTable.selectAll().forEach { edgeInTable ->
                val v1 = edgeInTable[EdgesTable.firstVertex].toString()
                val v2 = edgeInTable[EdgesTable.secondVertex].toString()
                val vElement = edgeInTable[EdgesTable.element]

                graph.addEdge(v1, v2, vElement)
            }
        }

        graphView.updateGraph(graph)
        FX.log.info("Reading graph from data base was finished (SQLLite)")
        return vertexInfo
    }

    override fun write(graphView: GraphView, fileName: String) {
        FX.log.info("Writing graph to data base was started (SQLLite)")
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
        FX.log.info("Writing graph to data base was finished (SQLLite)")
    }
}