package visualazer.view

import visualazer.controller.RandomPlacementStrategy
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.stage.FileChooser
import javafx.stage.StageStyle
import tornadofx.*
import visualazer.GraphIO
import java.io.File

class MainView : View("Graph visualazer.visualazer.view.view") {
    private val fileName = SimpleStringProperty()
    private val graph = GraphView(props.SAMPLE_GRAPH)
    private val strategy: RandomPlacementStrategy by inject()

    fun graph() = graph

    override val root = borderpane {

        top = hbox {
            menubar {
                menu("Settings") {
                    menu("File") {
                        item("Save to file") {
                            action {
                                openInternalWindow<PopUp>()
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
                            openInternalWindow<PopUp>()
                        }
                    }

                    button("Search main vertices") {
//                    TODO("Подумать, что сюда можно вписать")
                        tooltip("")
                        useMaxWidth = true
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

    init {
        arrangeVertexes()
    }

    private fun arrangeVertexes() {
        currentStage?.apply {
            strategy.place(width, height, graph.vertexes().values)
        }
    }

    private fun changeGraph() {

    }

    private fun List<File>.checkFileName(): String? {
        return when (this.size) {
            0 -> null
            else -> this[0].path
        }
    }
}


class PopUp: Fragment() {
    private val fileName = SimpleStringProperty()

    override val root = form {
        fieldset {
            field("Save to ") {
                textfield(fileName)
            }
            vbox {
                button("Save ") {
                    useMaxWidth = true
                    action {
                        saveGraphToFile(MainView().graph())
                        fileName.value = ""
                        close()
                    }
                }
                button("Cancel") {
                    useMaxWidth = true
                    action {
                        close()
                    }
                }
            }
        }
    }

    private fun saveGraphToFile(graphView: GraphView) {
        GraphIO().writeToFile(graphView, fileName.get())
    }
}