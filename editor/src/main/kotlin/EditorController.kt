package com.statlesscoder.klisp.editor

import com.statelesscoder.klisp.compiler.Token
import tornadofx.Controller

class EditorController(private val tokenSource: TokenSource) : Controller() {
    fun getTokens(text: String): Array<Token> {
        return tokenSource.getTokens(text)
    }
}