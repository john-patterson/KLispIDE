import javafx.scene.text.FontWeight
import tornadofx.*

fun main(args: Array<String>) {
    launch<HelloWorldApp>(args);
}

class HelloWorld : View() {
    override val root = hbox {
        label("Hello world")
    }
}


class HelloWorldApp : App(HelloWorld::class, Styles::class)

class Styles : Stylesheet() {
    init {
        label {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
            backgroundColor += c("#cecece")
        }
    }
}