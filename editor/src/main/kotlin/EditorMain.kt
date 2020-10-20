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
import java.lang.Exception
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


fun main(args: Array<String>) {
    val tokenSource: TokenSource = TokenAgent()
    setInScope(EditorController(tokenSource), FX.defaultScope, EditorController::class)
    launch<EditorApp>(args)
}

class EditorApp : App(EditorView::class, Styles::class, FX.defaultScope)

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
        error {
            fill = Color.WHITE
            fontWeight = FontWeight.BOLD
            backgroundColor += Color.RED
        }
        root {
            prefHeight = 600.px
            prefWidth = 800.px
            fontSize = 20.px
        }
    }
}

class EditorController(private val tokenSource: TokenSource) : Controller() {
    fun getTokens(text: String): Array<Token> {
        return tokenSource.getTokens(text)
    }
}

class EditorView: View() {
    private val codeArea: CodeArea = CodeArea()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val controller: EditorController by inject()
    private val label: Text = text("Editing 'Untitled' KLisp document.")
    private val errorLabel: Text = text() {
        cssclass("error")
        isVisible = false
    }

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
        bottom = errorLabel
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
        errorLabel.isVisible = false
        errorLabel.text = ""
        try {
            val tokens = controller.getTokens(text)

            var lastIdx = 0
            for (token in tokens) {
                val className = computeClassFromToken(token)
                builder.add(Collections.emptyList(), token.pos - lastIdx)
                builder.add(Collections.singleton(className), token.text.length)
                lastIdx = token.pos + token.text.length
            }

            return builder.create()
        } catch (e: TokenAgentException) {
            errorLabel.isVisible = true
            errorLabel.text = e.message
            return builder.create()
        }
    }
}


interface TokenSource {
    fun getTokens(text: String): Array<Token>
}

class TokenAgentException(message: String) : Exception(message) {
}

class TokenAgent: TokenSource {
    private val baseUrl = "http://localhost:7340/tokenize"

    override fun getTokens(text: String): Array<Token> {
        var tokenArray: Array<Token> = emptyArray()

        var shouldThrow: Boolean = false
        var errorString: String = ""

        val httpAsync = baseUrl
            .httpPost()
            .body(text)
            .responseString { _, response, result ->
                when (result) {
                    is Result.Failure -> {
                        shouldThrow = true
                        errorString = "Could not tokenize, got result code ${response.statusCode}: ${result.getException()}"
                    }
                    is Result.Success -> {
                        val data = result.get()
                        tokenArray = Gson().fromJson(data, Array<Token>::class.java)
                    }
                }
            }

        httpAsync.join()

        if (shouldThrow) {
            throw TokenAgentException(errorString)
        }

        return tokenArray
    }
}

