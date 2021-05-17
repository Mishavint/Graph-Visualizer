package visualizer.view

import visualizer.GraphIO
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.StageStyle
import tornadofx.*
import visualizer.controller.*
import java.io.File

class MainView : View("Graph visualizer") {
    private val fileName = SimpleStringProperty("DefaultName")
    private var graph = GraphView()
    private val strategy: RandomPlacementStrategy by inject()
    private val fa2 = FA2Controller()
    private val vertexController = VertexController()
    private val scrollController = ScrollController()

    override val root = borderpane {

        top = borderpane {
            left = menubar {
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

                                fa2.stop()
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
                                fileName.value = chooseFile(
                                    filters = arrayOf(
                                        FileChooser.ExtensionFilter(
                                            "SQLite", "*.sqlite", "*.sqlite3"
                                        )
                                    ),
                                    mode = FileChooserMode.Save,
                                ).checkFileName()
                                fileName.get()?.let { GraphIO().writeToSQLite(graph, it) }

                                fa2.stop()
                            }
                        }
                        item("Read from SQLite") {
                            action  {
                                fileName.value = chooseFile(
                                    filters = arrayOf(
                                        FileChooser.ExtensionFilter(
                                            "SQLite", "*.sqlite3", "*.sqlite"
                                        )
                                    ),
                                    mode = FileChooserMode.Single
                                ).checkFileName()

                                fileName.get()?.let {
                                    val vertexInfo = GraphIO().readFromSQLite(graph, fileName.get())
                                    drawNewGraph(vertexInfo)
                                }
                            }
                        }
                        separator()
                        item("Save to Neo4j") {
                            action {
                                GraphIO().writeToNeo4j(graph)

                                fa2.stop()
                            }
                        }
                        item("Read from Neo4j") {
                            action {
                                val vertexInfo = GraphIO().readFromNeo4j(graph)
                                drawNewGraph(vertexInfo)
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
            center = hbox {
                alignment = Pos.BOTTOM_CENTER
                label(fileName) {
                    font = Styles.InterMediumFont
                }
            }
        }

        left = borderpane {
            addClass(Styles.boxBorders)

            style {
                baseColor = Color.LIGHTGREY
            }

            center = vbox {
                titledpane("Community detection") {
                    expandedProperty().set(false)

                    label("Resolution:")

                    textfield("0.1").also {
                        add(it)
                        button("Start Leiden algorithm") {
                            action {
                                try {
                                    Algorithms(graph).communitiesDetection(it.text.toDouble())
                                } catch (ex:java.lang.NumberFormatException) {
                                    alert(Alert.AlertType.ERROR, "Please enter valid resolution")
                                    return@action
                                }
                            }
                        }
                    }
                }

                titledpane("Force Atlas 2") {
                    expandedProperty().set(false)

                    togglebutton("Run", null, false) {
                        useMaxWidth = true
                        action {
                            text = if(fa2.runFA()) "Stop"
                            else "Run"
                        }
                    }

                    button("Apply Settings") {
                        useMaxWidth = true
                        action {
                            fa2.applySettings()
                        }
                    }

                    titledpane("Settings") {
                        expandedProperty().set(false)

                        togglebutton("Barnes Hut Optimization", null, false) {
                            useMaxWidth = true
                            action {
                                fa2.barnesHutOptimize(!isDisabled)
                            }
                        }

                        togglebutton("Strong Gravity", null, false) {
                            useMaxWidth = true
                            action {
                                fa2.strongGravityMode(!isDisabled)
                            }
                        }

                        label("Speed")
                        textfield("1.0").also {
                            add(it)
                            button("set") {
                                action {
                                    try {
                                        if (it.text.toDouble() < 0.0)
                                            throw java.lang.NumberFormatException()
                                        fa2.speed(it.text.toDouble())
                                    } catch (ex: java.lang.NumberFormatException) {
                                        alert(Alert.AlertType.ERROR, "Please enter valid value")
                                        return@action
                                    }
                                }
                            }
                        }

                        label("Speed Efficiency")
                        textfield("1.0").also {
                            add(it)
                            button("set") {
                                action {
                                    try {
                                        if (it.text.toDouble() < 0.0)
                                            throw java.lang.NumberFormatException()
                                        fa2.speedEfficiency(it.text.toDouble())
                                    } catch (ex: java.lang.NumberFormatException) {
                                        alert(Alert.AlertType.ERROR, "Please enter valid value")
                                        return@action
                                    }
                                }
                            }
                        }

                        label("Jitter Tolerance")
                        textfield("1.0").also {
                            add(it)
                            button("set") {
                                action {
                                    try {
                                        if (it.text.toDouble() < 0.0)
                                            throw java.lang.NumberFormatException()
                                        fa2.jitterTolerance(it.text.toDouble())
                                    } catch (ex: java.lang.NumberFormatException) {
                                        alert(Alert.AlertType.ERROR, "Please enter valid value")
                                        return@action
                                    }
                                }
                            }
                        }

                        label("Scaling Ratio")
                        textfield("100.0").also {
                            add(it)
                            button("set") {
                                action {
                                    try {
                                        if (it.text.toDouble() < 0.0)
                                            throw java.lang.NumberFormatException()
                                        fa2.scalingRatio(it.text.toDouble())
                                    } catch (ex: java.lang.NumberFormatException) {
                                        alert(Alert.AlertType.ERROR, "Please enter valid value")
                                        return@action
                                    }
                                }
                            }
                        }

                        label("Gravity")
                        textfield("1.0").also {
                            add(it)
                            button("set") {
                                action {
                                    try {
                                        if (it.text.toDouble() < 0.0)
                                            throw java.lang.NumberFormatException()
                                        fa2.gravity(it.text.toDouble())
                                    } catch (ex: java.lang.NumberFormatException) {
                                        alert(Alert.AlertType.ERROR, "Please enter valid value")
                                        return@action
                                    }
                                }
                            }
                        }

                        label("Barnes Hut Theta")
                        textfield("1.0").also {
                            add(it)
                            button("set") {
                                action {
                                    try {
                                        if (it.text.toDouble() < 0.0)
                                            throw java.lang.NumberFormatException()
                                        fa2.barnesHutTheta(it.text.toDouble())
                                    } catch (ex: java.lang.NumberFormatException) {
                                        alert(Alert.AlertType.ERROR, "Please enter valid value")
                                        return@action
                                    }
                                }
                            }
                        }
                    }
                }

                titledpane("Centrality") {
                    expandedProperty().set(false)

                    button("Centrality") {
                        tooltip("Searching betweenness centrality")
                        useMaxWidth = true
                        action {
                            Algorithms(graph).mainVertexes()
                        }
                    }
                }
                titledpane("Appearance") {
                    expandedProperty().set(false)

                    button("Set black color to vertices") {
                        action {
                            vertexController.setBlackColor(graph.vertexes().values)
                        }
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

                button("Graph4 (34)") {
                    useMaxWidth = true
                    action {
                        GraphIO().readGraphEdges(graph, "graphs/soc-karate.mtx")
                        arrangeInCircle()
                    }
                }

                button("Graph5 (62)") {
                    useMaxWidth = true
                    action {
                        GraphIO().readGraphEdges(graph, "graphs/soc-dolphins.mtx")
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

        center {
            add(graph)

            setOnScroll {
                scrollController.scroll(it, this)
            }
        }
    }

    private fun arrangeInCircle() {
        currentStage?.apply {
            CircularPlacementStrategy().place(graph.width, graph.height, graph.vertexes().values)
        }

        fa2.stop()
        fa2.prepareFA2(graph)
    }

    private fun drawNewGraph(vertexInfo: MutableMap<String, GraphIO.VertexInfo>) {
        currentStage?.apply {
            FilePlacementStrategy().place(graph, vertexInfo)
        }

        fa2.stop()
        fa2.prepareFA2(graph)
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
}
