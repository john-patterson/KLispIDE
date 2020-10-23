package com.statelesscoder.klisp.editor

import com.statelesscoder.klisp.compiler.SimpleResult
import com.statelesscoder.klisp.compiler.Token
import tornadofx.Controller

class EditorController(private val tokenSource: TokenSource) : Controller() {
    fun getTokens(text: String): Array<Token> {
        return tokenSource.getTokens(text)
    }

    fun execute(text: String): Array<SimpleResult> {
        return tokenSource.execute(text)
    }
}