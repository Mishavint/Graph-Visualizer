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
                        GraphIO().writeToFile(MainView().graph(), fileName.get())
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
}