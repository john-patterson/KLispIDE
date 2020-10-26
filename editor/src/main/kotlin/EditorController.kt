package com.statelesscoder.klisp.editor

import com.statelesscoder.klisp.compiler.types.SimpleResult
import com.statelesscoder.klisp.compiler.types.Token
import tornadofx.Controller


class EditorController(private val tokenSource: TokenSource) : Controller() {
    private val codeAreaView: CodeAreaView by inject()
    private val resultsView: ResultsView by inject()
    private val scopeInspectorView: ScopeInspectorView by inject()

    fun getTokens(text: String): Array<Token> {
        return try {
            tokenSource.getTokens(text)
        } catch (e: TokenAgentException) {
            find<ErrorFragment>("errorText" to e.message.toString()).openModal()
            emptyArray()
        }
    }

    private fun execute(text: String): Array<SimpleResult> {
        return try {
            tokenSource.execute(text)
        } catch (e: TokenAgentException) {
            find<ErrorFragment>("errorText" to e.message.toString()).openModal()
            emptyArray()
        }
    }

    fun runCodeAndUpdateUi() {
        val code = codeAreaView.currentText()
        val result = execute(code)

        val newResults = result.map { Pair(it.expression, it.result) }
        resultsView.updateView(newResults)

        val newBindings = if (result.isNotEmpty()) {
            result.last().scope.entries.map { Pair(it.key, it.value) }
        } else {
            emptyList()
        }
        scopeInspectorView.updateView(newBindings)
    }
}