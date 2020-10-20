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
import org.reactfx.util.Try
import tornadofx.*
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Supplier

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
        }
        class_left_parens {
            fill = Color.GREEN
        }
        class_numeric {
            fill = Color.ORANGE
        }
        class_boolean {
            fill = Color.ORANGE
        }
        class_identifier {
            fill = Color.BLUE
        }
        class_let {
            fill = Color.PURPLE
        }
        class_if {
            fill = Color.PURPLE
        }
        class_fun {
            fill = Color.PURPLE
        }
        root {
            prefHeight = 600.px
            prefWidth = 800.px
        }
    }
}

class EditorView: View() {
    val codeArea: CodeArea = CodeArea()
    val executor: ExecutorService = Executors.newSingleThreadExecutor()
    val label: Text = text("No response")

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

    fun computeHighlightingAsync(): Task<StyleSpans<Collection<String>>> {
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
            .responseString { request, response, result ->
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
                            lastIdx = token.pos
                        }
                    }
                }
            }

        httpAsync.join()
        return builder.create()
    }

//    fun computeHighlightingAsync(): Task<String> {
//        val task = object : Task<String>() {
//            override fun call(): String {
//                return tokenize(codeArea.text)
//            }
//        }
//
//        executor.execute(task)
//        return task
//    }
//
//    fun tokenize(text: String): String {
//        var resultingTokens: String = ""
//        val httpAsync = "http://localhost:7340/tokenize"
//            .httpPost()
//            .body(text)
//            .responseString { request, response, result ->
//                when (result) {
//                    is Result.Failure -> {
//                        resultingTokens = result.getException().toString()
//                    }
//                    is Result.Success -> {
//                        resultingTokens = result.get()
//                    }
//                }
//            }
//
//        httpAsync.join()
//        return resultingTokens
//    }
}
