package visualizer.view

import tornadofx.*

class Kittens : View() {
    override val root = vbox {
        imageview("https://media.tenor.com/images/eff22afc2220e9df92a7aa2f53948f9f/tenor.gif")
        imageview("https://i.pinimg.com/originals/c6/52/b1/c652b110ce9854cef9ba399eed60417b.gif")

        button("Thanks, Misha") {
            useMaxWidth = true
            action {
                close()
            }
        }

        label("There should be some cute kittens, if you don't see them, please check your internet connection")

        with(modalStage) {
            minWidth = 600.0
            minHeight = 590.0
        }
    }
}