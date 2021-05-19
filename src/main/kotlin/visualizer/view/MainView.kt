package visualizer.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.paint.Color
import javafx.stage.*
import tornadofx.*
import visualizer.controller.*
import visualizer.model.*
import java.io.File
import kotlin.properties.Delegates

class MainView : View("Graph visualizer") {
    private val fileName = SimpleStringProperty("DefaultName")
    private var graph = GraphView()
    private lateinit var graphIOStrategy: GraphIOStrategy
    private val algorithms = Algorithms()
    private val fa2 = FA2Controller()
    private val vertexController = VertexController()
    private val scrollController = ScrollController()

    override val root = borderpane {

        top = borderpane {
            left = menubar {
                menu("File") {
                    item("Save to file") {
                        action {
                            fa2.stop()
                            log.info("Save button was clicked")
                            fileName.value = chooseFile(
                                title = "Save to",
                                filters = arrayOf(FileChooser.ExtensionFilter("Text files", "*.csv")),
                                mode = FileChooserMode.Save
                            ).checkFileName()
                            graphIOStrategy = FileIOStrategy()
                            fileName.get()?.let { graphIOStrategy.write(graph, it) }
                        }
                    }
                    item("Read from file") {
                        action {
                            log.info("Reading from file button was clicked")
                            fileName.value = chooseFile(
                                filters = arrayOf(
                                    FileChooser.ExtensionFilter(
                                        "Text files", "*.csv"
                                    )
                                )
                            ).checkFileName()
                            fileName.get()?.let {
                                graphIOStrategy = FileIOStrategy()
                                val vertexInfo = graphIOStrategy.read(graph, fileName.get())
                                drawNewGraph(vertexInfo)
                            }
                        }
                    }
                    separator()
                    item("Save to SQLite") {
                        action {
                            fa2.stop()
                            log.info("Button \"Save to SQLite was clicked\"")
                            fileName.value = chooseFile(
                                filters = arrayOf(
                                    FileChooser.ExtensionFilter(
                                        "SQLite", "*.sqlite", "*.sqlite3"
                                    )
                                ),
                                mode = FileChooserMode.Save,
                            ).checkFileName()
                            graphIOStrategy = SQLiteIOStrategy()
                            fileName.get()?.let { graphIOStrategy.write(graph, it) }
                        }
                    }
                    item("Read from SQLite") {
                        action  {
                            log.info("Button \"Read from SQLite was clicked\"")
                            fileName.value = chooseFile(
                                filters = arrayOf(
                                    FileChooser.ExtensionFilter(
                                        "SQLite", "*.sqlite3", "*.sqlite"
                                    )
                                ),
                                mode = FileChooserMode.Single
                            ).checkFileName()
                            fileName.get()?.let {
                                graphIOStrategy = SQLiteIOStrategy()
                                val vertexInfo = graphIOStrategy.read(graph, fileName.get())
                                drawNewGraph(vertexInfo)
                            }
                        }
                    }
                    separator()
                    item("Save to Neo4j") {
                        action {
                            log.info("Saving to Neo4j button was clicked")
                            graphIOStrategy = Neo4jIOStrategy()
                            graphIOStrategy.write(graph, "")

                            fa2.stop()
                        }
                    }
                    item("Read from Neo4j") {
                        action {
                            log.info("Reading from Neo4j button was clicked")
                            graphIOStrategy = Neo4jIOStrategy()
                            val vertexInfo = graphIOStrategy.read(graph, "")
                            drawNewGraph(vertexInfo)
                        }
                    }
                    separator()
                    item("Close") {
                        action {
                            primaryStage.close()
                        }
                    }
                }
                menu("Graphs") {

                    item("Graph1 (34)") {
                        useMaxWidth = true
                        action {
                            log.info("Button \"Graph4 (34)\" was clicked")
                            FileIOStrategy().readGraphEdges(graph, "graphs/soc-karate.mtx")
                            arrangeInCircle()
                        }
                    }

                    item("Graph2 (62)") {
                        useMaxWidth = true
                        action {
                            log.info("Button \"Graph5 (62)\" was clicked")
                            FileIOStrategy().readGraphEdges(graph, "graphs/soc-dolphins.mtx")
                            arrangeInCircle()
                        }
                    }

                    item("Graph3 (0.7K)") {
                        useMaxWidth = true
                        action {
                            log.info("Button \"Graph0 (0.7K)\" was clicked")
                            FileIOStrategy().readGraphEdges(graph, "graphs/fb-pages-food.edges")
                            arrangeInCircle()
                        }
                    }

                    item("Graph4 (0.9K)") {
                        useMaxWidth = true
                        action {
                            log.info("Button \"Graph1 (0.9K)\" was clicked")
                            FileIOStrategy().readGraphEdges(graph, "graphs/soc-wiki-Vote.mtx")
                            arrangeInCircle()
                        }
                    }

                    item("Graph5 (5K)") {
                        useMaxWidth = true
                        action {
                            log.info("Button \"Graph2 (5K)\" was clicked")
                            FileIOStrategy().readGraphEdges(graph, "graphs/soc-advogato.edges")
                            arrangeInCircle()
                        }
                    }

                    item("Graph6 (7K)") {
                        useMaxWidth = true
                        action {
                            log.info("Button \"Graph3 (7K)\" was clicked")
                            FileIOStrategy().readGraphEdges(graph, "graphs/soc-wiki-elec.edges")
                            arrangeInCircle()
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

                    textfield("0.05").also {
                        add(it)
                        button("Start Leiden algorithm") {
                            action {
                                log.info("Button \"Start Leiden algorithm\" was clicked")
                                try {
                                    if ( it.text.toDouble() < 0.0 )
                                        throw java.lang.NumberFormatException()
                                   algorithms.communitiesDetection(graph, it.text.toDouble())
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
                                log.info("FA2 barnesHutOptimize was changed")
                                fa2.barnesHutOptimize(!isDisabled)
                            }
                        }

                        togglebutton("Strong Gravity", null, false) {
                            useMaxWidth = true
                            action {
                                log.info("FA2 strongGravityMode was changed")
                                fa2.strongGravityMode(!isDisabled)
                            }
                        }

                        label("Speed")
                        textfield("1.0").also {
                            add(it)
                            button("set") {
                                action {
                                    log.info("FA2 speed is being changing")
                                    try {
                                        if (it.text.toDouble() < 0.0) {
                                            log.info("FA2 speed value was < 0.0")
                                            throw java.lang.NumberFormatException()
                                        }
                                        fa2.speed(it.text.toDouble())
                                        log.info("FA2 speed was changed")
                                    } catch (ex: java.lang.NumberFormatException) {
                                        log.info("FA2 speed value input was invalid")
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
                                    log.info("FA2 speed efficiency is being changing")
                                    try {
                                        if (it.text.toDouble() < 0.0) {
                                            log.info("FA2 speed efficiency value was < 0.0")
                                            throw java.lang.NumberFormatException()
                                        }
                                        fa2.speedEfficiency(it.text.toDouble())
                                        log.info("FA2 speed efficiency was changed")
                                    } catch (ex: java.lang.NumberFormatException) {
                                        log.info("FA2 speed efficiency value input was invalid")
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
                                    log.info("FA2 jitter tolerance is being changing")
                                    try {
                                        if (it.text.toDouble() < 0.0) {
                                            log.info("FA2 jitter tolerance value was < 0.0")
                                            throw java.lang.NumberFormatException()
                                        }
                                        fa2.jitterTolerance(it.text.toDouble())
                                        log.info("FA2 jitter tolerance was changed")
                                    } catch (ex: java.lang.NumberFormatException) {
                                        log.info("FA2 jitter tolerance value input was invalid")
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
                                    log.info("FA2 scaling ratio is being changing")
                                    try {
                                        if (it.text.toDouble() < 0.0) {
                                            log.info("FA2 scaling ratio value was < 0.0")
                                            throw java.lang.NumberFormatException()
                                        }
                                        fa2.scalingRatio(it.text.toDouble())
                                        log.info("FA2 scaling ratio was changed")
                                    } catch (ex: java.lang.NumberFormatException) {
                                        log.info("FA2 scaling ratio value input was invalid")
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
                                    log.info("FA2 gravity is being changing")
                                    try {
                                        if (it.text.toDouble() < 0.0) {
                                            log.info("FA2 gravity value was < 0.0")
                                            throw java.lang.NumberFormatException()
                                        }
                                        fa2.gravity(it.text.toDouble())
                                        log.info("FA2 gravity was changed")
                                    } catch (ex: java.lang.NumberFormatException) {
                                        log.info("FA2 gravity value input was invalid")
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
                                    log.info("FA2 barnes hut theta is being changing")
                                    try {
                                        if (it.text.toDouble() < 0.0) {
                                            log.info("FA2 barnes hut theta value was < 0.0")
                                            throw java.lang.NumberFormatException()
                                        }
                                        fa2.barnesHutTheta(it.text.toDouble())
                                        log.info("FA2 barnes hut theta was changed")
                                    } catch (ex: java.lang.NumberFormatException) {
                                        log.info("FA2 barnes hut theta input was invalid")
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
                    var counter = 0
                    var prevRadius by Delegates.notNull<Double>()
                    var coefficient by Delegates.notNull<Double>()

                    val slider = slider(1.0, 20.0, 6.0) {
                        isShowTickMarks = true
                        isShowTickLabels = true
                        isSnapToTicks = true
                        majorTickUnit = 2.0
                        blockIncrement = 1.0
                    }

                    button("Centrality") {
                        tooltip("Searching betweenness centrality")
                        useMaxWidth = true
                        action {
                            log.info("Button for search centrality was clicked")
                            if (graph.vertices().isNotEmpty()) {
                                prevRadius = graph.vertices().values.first().radius
                                if (counter == 0) {
                                    coefficient = slider.value
                                    algorithms.mainVertices(graph, coefficient)
                                }
                                counter ++
                            }
                        }
                    }

                    button("Reset Centrality") {
                        useMaxWidth = true
                        action {
                            log.info("Button \"Reset Centrality\" was clicked")
                            if (counter != 0) {
                                counter = 0
                               algorithms.resetCentrality(graph, prevRadius)
                            }
                        }
                    }
                }
                titledpane("Appearance") {
                    expandedProperty().set(false)

                    button("Set black color to vertices") {
                        useMaxWidth = true
                        action {
                            log.info("Button \"Set black color to vertices\" was clicked")
                            vertexController.setBlackColor(graph.vertices().values)
                        }
                    }

                    button("Increase radius") {
                        useMaxWidth = true
                        action {
                            vertexController.increaseRadius(graph.vertices().values)
                        }
                    }

                    button("Decrease radius") {
                        useMaxWidth = true
                        action {
                            vertexController.decreaseRadius(graph.vertices().values)
                        }
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
            CircularVerticesPlacement().place(graph.width, graph.height, graph.vertices().values)
        }

        fa2.stop()
        fa2.prepareFA2(graph)
    }

    private fun drawNewGraph(vertexInfo: MutableMap<String, GraphIOStrategy.VertexInfo>) {
        currentStage?.apply {
            FileVerticesPlacement().place(graph, vertexInfo)
        }

        fa2.stop()
        fa2.prepareFA2(graph)
    }

    private fun List<File>.checkFileName(): String? {
        return when (this.size) {
            0 -> null
            else -> this[0].path
        }
    }
}
