package com.statelesscoder.klisp.editor

import javafx.scene.control.TabPane
import tornadofx.*

class EditorView: View() {
    private val controller: EditorController by inject()
    private val codeAreaView: CodeAreaView by inject()


    override val root = borderpane {
        top = codeAreaView.root.apply {
            prefHeightProperty().bind(this@borderpane.heightProperty().divide(10) * 6)
        }

        center = tabpane {
            tab<ScopeInspectorView>()
            tab<ResultsView>()

            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            prefHeightProperty().bind(this@borderpane.heightProperty().divide(10) * 3)
        }

        bottom = hbox {
            button("Run") {
                action {
                    val result = controller.execute(codeAreaView.root.text)

                    val newResults = result.map { Pair(it.expression, it.result) }
                    find<ResultsView>().updateView(newResults)

                    val newBindings = result.last().scope.entries.map { Pair(it.key, it.value) }
                    find<ScopeInspectorView>().updateView(newBindings)
                }

            }
            prefHeightProperty().bind(this@borderpane.heightProperty().divide(10))
        }
    }
}

