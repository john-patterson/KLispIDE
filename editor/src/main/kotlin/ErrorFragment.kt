package com.statelesscoder.klisp.editor

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*

class ErrorFragment: Fragment("Error!") {
    private val errorText: String by param("You goofed")
    private val errorTextObservable = SimpleStringProperty(errorText)

    override val root = vbox {
        label {
            bind(errorTextObservable)
        }
        hbox {
            button("Close") {
                action {
                    this@ErrorFragment.close()
                }
            }
            alignment = Pos.CENTER
        }
    }
}