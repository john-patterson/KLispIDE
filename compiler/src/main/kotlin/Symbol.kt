package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.types.ExpressionPart
import com.statelesscoder.klisp.compiler.types.ExpressionPartType

data class Symbol(val symbolName: String)
    : ExpressionPart(ExpressionPartType.SYMBOL) {
    init {
        this.symbol = this
    }

    override fun toString(): String {
        return this.symbolName
    }
}