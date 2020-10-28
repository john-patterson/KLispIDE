package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.expressions.ExpressionPart

data class Symbol(val symbolName: String) : ExpressionPart() {

    override fun toString(): String {
        return this.symbolName
    }
}