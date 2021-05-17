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

    fun mainVertexes() {
        log.info("Searching of centrality started")
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
                            (numOfShortestPaths[it]!!.toDouble() / numOfShortestPaths[w]!!.toDouble()) *
                            (1 + shareShortestPaths[w]!!))
                }

                if (w != s) {
                    centralityCoefficient[w] = centralityCoefficient[w]!! + shareShortestPaths[w]!!
                }
            }
        }
        print(centralityCoefficient)
        graphView.vertexes().forEach {
            val normalizedValue = centralityCoefficient[it.key]!! / (vertexes.size * vertexes.size / 2)
            it.value.reBindRadiusProperty(doubleProperty(it.value.radius + 3 * normalizedValue))
        }
        log.info("Searching of centrality was finished")
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
        graph.vertexes().forEach {
            countedVertices[it] = false
            listOfVertices[it.element] = count++
        }

        csvWriter().open(fileBeforeLeidenAlg) {
            graph.edges().forEach {
                if (it.vertexes.first != it.vertexes.second)
                    writeRow("${listOfVertices[it.vertexes.first.element]}\t${listOfVertices[it.vertexes.second.element]}")


                countedVertices[it.vertexes.first] = true
                countedVertices[it.vertexes.second] = true
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

        graphView.vertexes().values.forEach {
            if (countedVertices[it.vertex] == true)
                it.color = mapOfColorsToVertices[count++]!!
        }
        log.info("Communities detection was finished")
    }
}