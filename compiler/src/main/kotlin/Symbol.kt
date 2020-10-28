package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.types.ExpressionPart
import com.statelesscoder.klisp.compiler.types.ExpressionPartType

data class Symbol(val symbolName: String) : ExpressionPart() {

    override fun toString(): String {
        return this.symbolName
    }
}