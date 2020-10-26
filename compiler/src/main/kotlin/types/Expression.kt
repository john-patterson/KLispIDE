package com.statelesscoder.klisp.compiler.types

data class Expression(val head: ExpressionPart, val tail: List<ExpressionPart>) {
    override fun toString(): String {
        val headString = head.toString()
        val tailString = tail.joinToString(separator = " ") { it.toString() }
        return "($headString $tailString)"
    }
}