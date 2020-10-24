package com.statelesscoder.klisp.editor

import com.statelesscoder.klisp.compiler.SimpleResult
import com.statelesscoder.klisp.compiler.Token
import tornadofx.Controller

class EditorController(private val tokenSource: TokenSource) : Controller() {
    private val codeAreaView: CodeAreaView by inject()
    private val resultsView: ResultsView by inject()
    private val scopeInspectorView: ScopeInspectorView by inject()

    fun getTokens(text: String): Array<Token> {
        return tokenSource.getTokens(text)
    }

    private fun execute(text: String): Array<SimpleResult> {
        return tokenSource.execute(text)
    }

    fun runCodeAndUpdateUi() {
        val code = codeAreaView.currentText()
        val result = execute(code)

        val newResults = result.map { Pair(it.expression, it.result) }
        resultsView.updateView(newResults)

        val newBindings = result.last().scope.entries.map { Pair(it.key, it.value) }
        scopeInspectorView.updateView(newBindings)
    }
}