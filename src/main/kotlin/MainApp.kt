import View.MainView
import javafx.stage.Stage
import tornadofx.*

class MainApp : App(MainView::class) {

    override fun start(stage: Stage) {

        with(stage) {
            width = 1200.0
            height = 600.0
        }
        super.start(stage)
    }
}

fun main() {
    launch<MainApp>()
}