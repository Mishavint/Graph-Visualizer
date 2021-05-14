package visualizer.view

import visualizer.controller.RandomPlacementStrategy
import visualizer.GraphIO
import visualizer.controller.FilePlacementStrategy
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.stage.FileChooser
import javafx.stage.StageStyle
import tornadofx.*
import visualizer.controller.CircularPlacementStrategy
import visualizer.controller.FA2Controller
import java.io.File

class MainView : View("Graph visualizer") {
    private val fileName = SimpleStringProperty()
    private var graph = GraphView()
    private val strategy: RandomPlacementStrategy by inject()

    override val root = borderpane {

        top = hbox {
            menubar {
                menu("Settings") {
                    menu("File") {
                        item("Save to file") {
                            action {
                                fileName.value = chooseFile(
                                    title = "Save to",
                                    filters = arrayOf(FileChooser.ExtensionFilter("Text files", "*.csv")),
                                    mode = FileChooserMode.Save
                                ).checkFileName()
                                GraphIO().writeToFile(graph, fileName.get())
                            }
                        }
                        item("Read from file") {
                            action {
                                fileName.value = chooseFile(
                                    filters = arrayOf(
                                        FileChooser.ExtensionFilter(
                                            "Text files", "*.csv"
                                        )
                                    )
                                ).checkFileName()
                                val vertexInfo = GraphIO().readFromFile(graph, fileName.get())
                                drawNewGraph(vertexInfo)
                            }
                        }
                        separator()
                        item("Save to SQLite") {
                            action {

                            }
                        }
                        item("Read from SQLite") {
                            action {

                            }
                        }
                        separator()
                        item("Save to Neo4j") {
                            action {

                            }
                        }
                        item("Read from Neo4j") {
                            action {

                            }
                        }
                    }
                    separator()
                    item("Reset") {
                        action {
                            fileName.value = ""
                            arrangeVertexes()
                            modalStage?.close()
                        }
                    }
                    item("Close") {
                        action {
                            primaryStage.close()
                        }
                    }
                }
            }
            addClass(Styles.boxBordersForMenu)
        }

        left = borderpane {

            center = borderpane {
                addClass(Styles.boxBorders)

                center = vbox {
                    spacing = 10.0
                    alignment = Pos.CENTER_LEFT
                    button("Search communities") {
                        tooltip("Leiden algorithm")
                        useMaxWidth = true
                        action {
                            Algorithms(graph).searchCommunities()
                        }
                    }

                    button("Search main vertices") {
                        tooltip("Searching betweenness centrality")
                        useMaxWidth = true
                        action {
                            Algorithms(graph).mainVertexes()
                        }
                    }

                    button("Graph0 (0.7K)") {
                        useMaxWidth = true
                        action {
                            GraphIO().readGraphEdges(graph, "graphs/fb-pages-food.edges")
                            arrangeInCircle()
                        }
                    }

                    button("Graph1 (0.9K)") {
                        useMaxWidth = true
                        action {
                            GraphIO().readGraphEdges(graph, "graphs/soc-wiki-Vote.mtx")
                            arrangeInCircle()
                        }
                    }

                    button("Graph2 (5K)") {
                        useMaxWidth = true
                        action {
                            GraphIO().readGraphEdges(graph, "graphs/soc-advogato.edges")
                            arrangeInCircle()
                        }
                    }

                    button("Graph3 (7K)") {
                        useMaxWidth = true
                        action {
                            GraphIO().readGraphEdges(graph, "graphs/soc-wiki-elec.edges")
                            arrangeInCircle()
                        }
                    }

                    button("Graph4 (34)") {
                        useMaxWidth = true
                        action {
                            GraphIO().readGraphEdges(graph, "graphs/soc-karate.mtx")
                            arrangeInCircle()
                        }
                    }

                    button("Graph5 (0.7K)") {
                        useMaxWidth = true
                        action {
                            GraphIO().readGraphEdges(graph, "graphs/soc-dolphins.mtx")
                            arrangeInCircle()
                        }
                    }

                    button("Init FA2") {
                        tooltip("ALWAYS used right after reading/creating graph")
                        useMaxWidth = true
                        action {
                            initFA2()
                        }
                    }

                    button("RUN/STOP FA2") {
                        useMaxWidth = true
                        action {
                            fa2.runFA()
                        }
                    }
                }
                bottom = hbox {
                    spacing = 2.0

                    button("Full Screen") {
                        tooltip(
                            "${if (primaryStage.isFullScreen) "close Full screen" else "enter Full screen"}\n" +
                                    "Or just press f11 button"
                        )
                        action {
                            with(primaryStage) { isFullScreen = !isFullScreen }
                        }
                        shortcut("F11")
                    }

                    button("Kittens") {
                        action {
                            find<Kittens>().openModal(stageStyle = StageStyle.UTILITY)
                        }
                    }
                }
            }
        }
        center {
            add(graph)
        }
    }

    private fun arrangeInCircle() {
        currentStage?.apply {
            CircularPlacementStrategy().place(graph.width, graph.height, graph.vertexes().values)
        }
    }

    private fun drawNewGraph(vertexInfo: MutableMap<String, GraphIO.VertexInfo>) {
        currentStage?.apply {
            FilePlacementStrategy().place(graph, vertexInfo)
        }
    }

    private fun arrangeVertexes() {
        currentStage?.apply {
            strategy.place(graph.width, graph.height, graph.vertexes().values)
        }
    }

    private fun List<File>.checkFileName(): String? {
        return when (this.size) {
            0 -> null
            else -> this[0].path
        }
    }

    private lateinit var fa2: FA2Controller
    private fun initFA2() {
        fa2 = FA2Controller(graph, 100.0)
    }
}


