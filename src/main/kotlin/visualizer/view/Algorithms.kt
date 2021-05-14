package visualizer.view

import tornadofx.doubleProperty
import visualizer.model.Vertex
import java.util.*

class Algorithms(private val graphView: GraphView) {

    fun mainVertexes() {
        val lstAdj = graphView.graph().listOfAdjacency()
        val vertexes = graphView.graph().vertexes()

        val queue: Queue<Vertex> = LinkedList()
        val stack = Stack<Vertex>()

        val centralityCoefficient = vertexes.associateWith {
            0.0
        }.toMutableMap()

        vertexes.forEach { s ->

            val pred = vertexes.associateWith {
                mutableListOf<Vertex>()
            }.toMutableMap()

            val dist = vertexes.associateWith {
                Long.MAX_VALUE
            }.toMutableMap()

            val numOfShortestPaths = vertexes.associateWith {
                0
            }.toMutableMap()

            dist[s] = 0
            numOfShortestPaths[s] = 1
            queue.add(s)

            while (!queue.isEmpty()) {
                val v = queue.remove()
                stack.push(v)

                lstAdj[v]!!.forEach {
                    if (dist[it] == Long.MAX_VALUE) {
                        dist[it] = dist[v]!! + 1
                        queue.add(it)
                    }

                    if (dist[it] == dist[v]!! + 1) {
                        numOfShortestPaths[it] = numOfShortestPaths[it]!! + numOfShortestPaths[v]!!
                        pred[it]!!.add(v)
                    }
                }
            }


            val shareShortestPaths = vertexes.associateWith {
                0.0
            }.toMutableMap()

            while (!stack.isEmpty()) {
                val w = stack.pop()

                pred[w]!!.forEach {
                    shareShortestPaths[it] = (shareShortestPaths[it]!! +
                            (numOfShortestPaths[it]!!.toDouble()/numOfShortestPaths[w]!!.toDouble())  *
                            (1 + shareShortestPaths[w]!!))
                }

                if (w != s) {
                    centralityCoefficient[w] = centralityCoefficient[w]!! + shareShortestPaths[w]!!
                }
            }
        }
        graphView.vertexes().forEach {
            val normalizedValue = centralityCoefficient[it.key]!! / (vertexes.size * vertexes.size / 2)
            it.value.reBindRadiusProperty(doubleProperty(it.value.radius + 8 * normalizedValue))
        }
    }
}