package com.statelesscoder.klisp.compiler.types

data class KList(val items: List<ExpressionPart>) {
    override fun toString(): String {
        val itemString = items.joinToString(separator = " ") { it.toString() }
        return "[$itemString]"
    }
}