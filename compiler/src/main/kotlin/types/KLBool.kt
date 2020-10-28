package com.statelesscoder.klisp.compiler.types

data class KLBool(val truth: Boolean) : KLLiteralValue() {
    override fun toString(): String {
        return truth.toString()
    }
}