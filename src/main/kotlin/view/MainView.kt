package view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.StageStyle
import tornadofx.*
import java.io.File

class MainView : View("Graph view") {
    private var fileName = SimpleStringProperty("")

    override val root = borderpane {

        left = borderpane {
            top = hbox {
                menubar {
                    menu("Settings") {
                        menu("File") {
                            item("Save to file")
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
            center = borderpane {
                addClass(Styles.boxBorders)

                center = vbox {
                    spacing = 10.0
                    alignment = Pos.CENTER_LEFT
                    button("Search communities") {
                        tooltip("Leiden algorithm")
                        useMaxWidth = true
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
        center = borderpane {
            top = hbox {
                minHeight = 27.0
                addClass(Styles.boxBordersForMenu)
                label(fileName) {
                    font = Styles.InterMediumFont
                }
            }
            center = vbox {
                addClass(Styles.boxBorders)

            }
        }
    }

    private fun List<File>.checkFileName(): String? {
        return when (this.size) {
            0 -> null
            else -> this[0].path
        }
    }
}