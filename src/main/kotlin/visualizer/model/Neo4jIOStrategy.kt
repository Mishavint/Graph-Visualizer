package visualizer.model

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import tornadofx.FX
import tornadofx.c
import visualizer.view.GraphView

class Neo4jIOStrategy: GraphIOStrategy {

    object Neo4jConnectionTicket {
        var uri = "bolt://3.86.244.161:7687"
        var username = "neo4j"
        var password = "eighths-career-policy"
    }

    override fun read(graphView: GraphView, fileName: String): MutableMap<String, GraphIOStrategy.VertexInfo> {
        FX.log.info("Reading graph from data base was started (Neo4j)")
        val vertexInfo = mutableMapOf<String, GraphIOStrategy.VertexInfo>()
        val graph = UndirectedGraph()

        val driver = GraphDatabase.driver(
            Neo4jConnectionTicket.uri, AuthTokens.basic(
                Neo4jConnectionTicket.username,
                Neo4jConnectionTicket.password
            ))
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
                vertexInfo[v] = GraphIOStrategy.VertexInfo(vx, vy, vRadius, vColor)
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
        FX.log.info("Reading graph from data base was finished (Neo4j)")
        return vertexInfo
    }

    override fun write(graphView: GraphView, fileName: String) {
        FX.log.info("Writing graph to data base was started (Neo4j)")
        val driver = GraphDatabase.driver(
            Neo4jConnectionTicket.uri, AuthTokens.basic(
                Neo4jConnectionTicket.username,
                Neo4jConnectionTicket.password
            ))
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
        FX.log.info("Writing graph to data base was finished (Neo4j)")
    }

}