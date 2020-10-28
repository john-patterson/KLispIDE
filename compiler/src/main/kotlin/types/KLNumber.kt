package com.statelesscoder.klisp.compiler.types

data class KLNumber(val value: Float) : KLLiteralValue() {
    override fun toString(): String {
        return value.toString()
    }
}