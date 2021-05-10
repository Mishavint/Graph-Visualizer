package view

import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {

    companion object {
        val boxBorders by cssclass()
        val boxBordersForMenu by cssclass()
        val InterMediumFont = loadFont("/fonts/InterMedium.otf" , 15)
    }

    init {
        boxBorders {
            borderColor += box(Color.BLACK)
        }
        boxBordersForMenu {
            borderColor += box(left = Color.BLACK , right = Color.BLACK , bottom = Color.BLACK , top = Color.WHITE)
        }
    }
}