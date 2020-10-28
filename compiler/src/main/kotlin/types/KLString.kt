package com.statelesscoder.klisp.compiler.types

data class KLString(val text: String) : KLLiteralValue() {
    override fun toString(): String {
        return "\"$text\""
    }
}