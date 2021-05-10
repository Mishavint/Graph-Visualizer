package view

import javafx.geometry.Pos
import javafx.stage.FileChooser
import javafx.stage.StageStyle
import tornadofx.*
import java.io.File

class MainView : View("Graph view") {
    var fileName : String? = null

    override val root = borderpane {

        left = borderpane {
            top = vbox {
                addClass(Styles.boxBorders)
                fillWidthProperty()

                hbox {
                    button("Save to file")
                    button("Read from file") {
                        action {
                            fileName = chooseFile(
                                filters = arrayOf(
                                    FileChooser.ExtensionFilter(
                                        "Text files", "*.csv"
                                    )
                                )
                            ).checkFileName()
                        }
                    }
                }
                hbox {
                    button("Save to SQLite")
                    button("Read from SQLite")
                }
                hbox {
                    button("Save to Neo4j")
                    button("Read from Neo4j")
                }
            }

            center = vbox {
                spacing = 10.0
                addClass(Styles.boxBorders)
                alignment = Pos.CENTER_LEFT
                button("Search communities") {
                    useMaxWidth = true
                }

                button("Search main vertices") {
                    useMaxWidth = true
                }
            }
        }



        center = borderpane {
            center = vbox {
                addClass(Styles.boxBorders)

            }
            bottom = hbox {
                addClass(Styles.boxBorders)
                spacing = 2.0

                button("Full Screen") {
                    tooltip("${if (primaryStage.isFullScreen) "close Full screen" else "enter Full screen"}\n" +
                            "Or just press f11 button")
                    action {
                        with(primaryStage) {isFullScreen = !isFullScreen}
                    }
                    shortcut("F11")
                }

                button("Kittens") {
                    action {
                        find<Kittens>().openModal(stageStyle = StageStyle.UTILITY )
                    }
                }
            }
        }
    }

    fun List<File>.checkFileName(): String? {
        return when(this.size) {
            0 -> null
            else -> this[0].path
        }
    }
}