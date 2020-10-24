package com.statelesscoder.klisp.editor

import com.statelesscoder.klisp.compiler.runCode
import javafx.scene.control.TabPane
import tornadofx.*

class EditorView: View() {
    private val controller: EditorController by inject()
    private val codeAreaView: CodeAreaView by inject()

    override val root = borderpane {
        top = menubar {
            menu("Run") {
                item("Run entire file") {
                    action {
                        controller.runCodeAndUpdateUi()
                    }
                }
            }
        }

        center = codeAreaView.root.apply {
            prefHeightProperty().bind(this@borderpane.heightProperty().divide(10) * 6)
        }

        bottom = tabpane {
            tab<ScopeInspectorView>()
            tab<ResultsView>()

            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            prefHeightProperty().bind(this@borderpane.heightProperty().divide(10) * 3)
        }
    }

}

