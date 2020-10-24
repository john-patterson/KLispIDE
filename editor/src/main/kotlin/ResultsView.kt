package com.statelesscoder.klisp.editor

import tornadofx.View
import tornadofx.asObservable
import tornadofx.readonlyColumn
import tornadofx.tableview

class ResultsView : View("Results Viewer") {
    private var results = mutableListOf<Pair<String, String>>()

    override val root = tableview(results.asObservable()) {
        readonlyColumn("Expression", Pair<String, String>::first) {
            prefWidthProperty().bind(this@tableview.widthProperty().divide(2))
        }
        readonlyColumn("Result", Pair<String, String>::second) {
            prefWidthProperty().bind(this@tableview.widthProperty().divide(2))
        }
    }

    fun updateView(results: List<Pair<String, String>>) {
        this.results.clear()
        this.results.addAll(results)
        this.root.refresh()
    }
}