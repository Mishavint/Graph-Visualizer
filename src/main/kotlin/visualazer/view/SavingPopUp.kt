package visualazer.view

import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import visualazer.GraphIO

class SavingPopUp: Fragment() {
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