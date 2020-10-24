package com.statelesscoder.klisp.editor

import tornadofx.*

class ScopeInspectorView : View("Scope Inspector") {
    private var bindings = mutableListOf<Pair<String, String>>()

    override val root = tableview(bindings.asObservable()) {
        readonlyColumn("Name", Pair<String, String>::first) {
            prefWidthProperty().bind(this@tableview.widthProperty().divide(4))
        }
        readonlyColumn("Value", Pair<String, String>::second) {
            prefWidthProperty().bind(this@tableview.widthProperty().divide(4) * 3)
        }
    }

    fun updateView(bindings: List<Pair<String, String>>) {
        this.bindings.clear()
        this.bindings.addAll(bindings)
        this.root.refresh()
    }
}