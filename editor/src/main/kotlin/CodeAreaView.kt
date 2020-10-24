package com.statelesscoder.klisp.editor

import com.statelesscoder.klisp.compiler.Token
import javafx.concurrent.Task
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import tornadofx.View
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CodeAreaView: View("Code Area") {
    private val controller: EditorController by inject()
    private val executor: ExecutorService =
        Executors.newSingleThreadExecutor()
    private val codeArea: CodeArea =
        CodeArea()
    override val root = codeArea.apply {
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