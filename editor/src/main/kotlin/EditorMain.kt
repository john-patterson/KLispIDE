package com.statlesscoder.klisp.editor
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.statelesscoder.klisp.compiler.Token
import javafx.concurrent.Task
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
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

fun main(args: Array<String>) {
    launch<EditorApp>(args)
}

class EditorApp : App(EditorView::class, Styles::class)

class Styles : Stylesheet() {
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
        root {
            prefHeight = 600.px
            prefWidth = 800.px
            fontSize = 20.px
        }
    }
}

class EditorView: View() {
    private val codeArea: CodeArea = CodeArea()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val label: Text = text("Editing 'Untitled' KLisp document.")

    override val root = borderpane {
        top = label
        center = codeArea.apply {
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

    fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
        var builder = StyleSpansBuilder<Collection<String>>()
        val httpAsync = "http://localhost:7340/tokenize"
            .httpPost()
            .body(text)
            .responseString { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        val data = result.get()
                        val tokens = Gson().fromJson(data, Array<Token>::class.java)
                        var lastIdx = 0
                        for (token in tokens) {
                            val className = "class_${token.type.toString().toLowerCase()}"
                            builder.add(Collections.emptyList(), token.pos - lastIdx)
                            builder.add(Collections.singleton(className), token.text.length)
                            lastIdx = token.pos + 1
                        }
                    }
                }
            }

        httpAsync.join()
        return builder.create()
    }
}

