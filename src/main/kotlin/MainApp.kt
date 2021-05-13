import javafx.stage.Stage
import tornadofx.*
import visualizer.view.MainView
import visualizer.view.Styles

class MainApp : App(MainView::class, Styles::class) {

    override fun start(stage: Stage) {

        with(stage) {
            width = 1200.0
            height = 670.0

            minWidth = 400.0
            minHeight = 400.0

            maxWidth = 1920.0
            maxHeight = 1080.0
        }
        super.start(stage)
    }
}

fun main() {
    launch<MainApp>()
}