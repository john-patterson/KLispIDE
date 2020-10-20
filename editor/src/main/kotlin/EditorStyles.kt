package com.statlesscoder.klisp.editor

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.px

class EditorStyles : Stylesheet() {
    companion object {
        val command by cssclass()
        val class_right_parens by cssclass()
        val class_left_parens by cssclass()
        val class_numeric by cssclass()
        val class_boolean by cssclass()
        val class_identifier by cssclass()
        val class_let by cssclass()
        val class_if by cssclass()
        val class_fun by cssclass()
        val error by cssclass()
    }

    init {
        command {
            fill = Color.RED
            fontSize = 40.px
            fontWeight = FontWeight.BOLD
        }
        class_right_parens {
            fill = Color.GREEN
            fontWeight = FontWeight.BOLD
        }
        class_left_parens {
            fill = Color.GREEN
            fontWeight = FontWeight.BOLD
        }
        class_numeric {
            fill = Color.ORANGE
            fontWeight = FontWeight.BOLD
        }
        class_boolean {
            fill = Color.ORANGE
            fontWeight = FontWeight.BOLD
        }
        class_identifier {
            fill = Color.BLUE
            fontWeight = FontWeight.BOLD
        }
        class_let {
            fill = Color.PURPLE
            fontWeight = FontWeight.BOLD
        }
        class_if {
            fill = Color.PURPLE
            fontWeight = FontWeight.BOLD
        }
        class_fun {
            fill = Color.PURPLE
            fontWeight = FontWeight.BOLD
        }
        error {
            fill = Color.WHITE
            fontWeight = FontWeight.BOLD
        }
        root {
            prefHeight = 600.px
            prefWidth = 800.px
            fontSize = 20.px
        }
    }
}