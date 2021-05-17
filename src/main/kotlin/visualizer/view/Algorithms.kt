package visualizer.view

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import tornadofx.doubleProperty
import visualizer.model.*
import java.util.*
import nl.cwts.networkanalysis.run.RunNetworkClustering
import javafx.scene.paint.Color
import tornadofx.FX.Companion.log
import kotlin.random.Random

class Algorithms(private val graphView: GraphView) {

    fun mainVertices(coefficient: Double) {
        log.info("Searching of centrality started")
        val lstAdj = graphView.graph().listOfAdjacency()
        val vertexes = graphView.graph().vertices()

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
                            (numOfShortestPaths[it]!!.toDouble() / numOfShortestPaths[w]!!.toDouble()) *
                            (1 + shareShortestPaths[w]!!))
                }

                if (w != s) {
                    centralityCoefficient[w] = centralityCoefficient[w]!! + shareShortestPaths[w]!!
                }
            }
        }
        graphView.vertices().forEach {
            val normalizedValue = centralityCoefficient[it.key]!! / (vertexes.size * vertexes.size / 2)
            it.value.rebindRadiusProperty(doubleProperty(it.value.radius + coefficient * normalizedValue))
        }
        log.info("Searching of centrality was finished")
    }

    fun resetCentrality(prevRadius: Double) {
        log.info("Resetting centrality was started")
        graphView.vertices().forEach {
            it.value.rebindRadiusProperty(doubleProperty(prevRadius))
        }
        log.info("Resetting centrality was finished")
    }

    private fun randomColor(): Color = Color.rgb(
        Random.nextInt(0, 255),
        Random.nextInt(0, 255),
        Random.nextInt(0, 255)
    )

    fun communitiesDetection(resolution: Double = 0.14) {
      
        log.info("Communities detection was started")
      
        if( graphView.vertexes().isEmpty() ) {
         log.info("Graph is empty")
          return
        }
      
        val graph = graphView.graph()
        val fileBeforeLeidenAlg = "tmp/fileBeforeLeidenAlg.csv"
        val fileAfterLeidenAlg = "tmp/fileAfterLeidenAlg.csv"

        val listOfVertices: MutableMap<String, Int> = mutableMapOf()
        val countedVertices: MutableMap<Vertex, Boolean> = mutableMapOf()
        var count = 0
        graph.vertices().forEach {
            countedVertices[it] = false
            listOfVertices[it.element] = count++
        }

        csvWriter().open(fileBeforeLeidenAlg) {
            graph.edges().forEach {
                if (it.vertices.first != it.vertices.second)
                    writeRow("${listOfVertices[it.vertices.first.element]}\t${listOfVertices[it.vertices.second.element]}")


                countedVertices[it.vertices.first] = true
                countedVertices[it.vertices.second] = true
            }
            close()
        }

        val args = arrayOf("-r", "$resolution", "-o", fileAfterLeidenAlg, fileBeforeLeidenAlg)
        RunNetworkClustering.main(args)

        val mapOfColors: MutableMap<Int, Color> = mutableMapOf()
        val includedColors: MutableMap<Int, Boolean> = mutableMapOf()

        count = 0

        val mapOfColorsToVertices: MutableMap<Int, Color> = mutableMapOf()

        csvReader().open(fileAfterLeidenAlg) {
            var line: List<String>? = readNext()
            while (line != null) {
                val column = line[0]

                val digits = column.split("\t")

                val indexForMap = digits[1].toInt()

                if (includedColors[indexForMap] != true) {
                    includedColors[indexForMap] = true
                    mapOfColors[indexForMap] = randomColor()
                }

                mapOfColorsToVertices[count++] = mapOfColors[indexForMap]!!

                line = readNext()
            }
        }

        count = 0

        graphView.vertices().values.forEach {
            if (countedVertices[it.vertex] == true)
                it.color = mapOfColorsToVertices[count++]!!
        }
        log.info("Communities detection was finished")
    }
}