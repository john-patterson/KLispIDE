package com.statelesscoder.klisp.editor

import com.statelesscoder.klisp.compiler.Data
import com.statelesscoder.klisp.compiler.Token
import javafx.beans.Observable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.text.Text
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import tornadofx.*
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EditorView: View() {
    private val codeArea: CodeArea =
        CodeArea()
    private val executor: ExecutorService =
        Executors.newSingleThreadExecutor()
    private val controller: EditorController by inject()
    private var bindings = mutableListOf<Pair<String, String>>()
    private var showScopeView = SimpleBooleanProperty(false)
    private var scopeViewOffset = SimpleIntegerProperty(3)

    override val root = borderpane {
        val scopeView = tableview(bindings.asObservable()) {
            readonlyColumn("Name", Pair<String, String>::first) {
                prefWidthProperty().bind(this@tableview.widthProperty().divide(4))
            }
            readonlyColumn("Value", Pair<String, String>::second) {
                prefWidthProperty().bind(this@tableview.widthProperty().divide(4) * 3)
            }

            prefHeightProperty().bind(this@borderpane
                .heightProperty()
                .divide(10) * scopeViewOffset.get())
            visibleWhen(showScopeView)
        }

        top = codeArea.apply {
            paragraphGraphicFactory = LineNumberFactory.get(this)
            multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .supplyTask { computeHighlightingAsync() }
                .awaitLatest(codeArea.multiPlainChanges())
                .filterMap {
                    if (it.isSuccess) {
                        Optional.of(it.get())
                    } else {
                        Optional.empty()
                    }
                }
                .subscribe {
                    setStyleSpans(0, it)
                }
            prefHeightProperty().bind(this@borderpane
                .heightProperty()
                .divide(10) * (9 - scopeViewOffset.get()))
        }

        center = scopeView
        bottom = hbox {
            button("Run") {
                action {
                    val result = controller.execute(codeArea.text)
                    bindings.clear()
                    bindings.addAll(result.scope.entries.map { Pair(it.key, it.value) })
                    scopeView.refresh()
                }

            }
            button ("Show Scope Inspector") {
                action {
                    if (showScopeView.get()) {
                        showScopeView.set(false)
                        this.text = "Show Scope Inspector"
                    } else {
                        showScopeView.set(true)
                        this.text = "Show Run Results"
                    }
                }
            }
            prefHeightProperty().bind(this@borderpane.heightProperty().divide(10))
        }

    }

    private fun computeHighlightingAsync(): Task<StyleSpans<Collection<String>>> {
        val task = object : Task<StyleSpans<Collection<String>>>() {
            override fun call(): StyleSpans<Collection<String>> {
                return computeHighlighting(codeArea.text)
            }
        }

        executor.execute(task)
        return task
    }

    private fun computeClassFromToken(token: Token): String {
        return "class_${token.type.toString().toLowerCase()}"
    }

    fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
        var builder = StyleSpansBuilder<Collection<String>>()
        val tokens = controller.getTokens(text)

        var lastIdx = 0
        for (token in tokens) {
            val className = computeClassFromToken(token)
            builder.add(Collections.emptyList(), token.pos - lastIdx)
            builder.add(Collections.singleton(className), token.text.length)
            lastIdx = token.pos + token.text.length
        }

        return builder.create()
    }
}

