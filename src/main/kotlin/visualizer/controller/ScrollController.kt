package visualizer.controller

import javafx.scene.input.ScrollEvent
import javafx.scene.layout.BorderPane
import tornadofx.*
import visualizer.view.MainView

class ScrollController : Controller() {

    fun scroll(e: ScrollEvent, mainScene : BorderPane) {
        val delta = e.deltaY / 1000

        println("scaleX = ${mainScene.scaleX} .. scaleX+delta = ${mainScene.scaleX+delta}")
        println("scaleY = ${mainScene.scaleY} .. scaleY+delta = ${mainScene.scaleY+delta}")

        if (mainScene.center.scaleX + delta >= 0) mainScene.center.scaleX += delta
        if (mainScene.center.scaleY + delta >= 0) mainScene.center.scaleY += delta
    }
}