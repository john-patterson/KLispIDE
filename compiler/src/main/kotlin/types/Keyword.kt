package com.statelesscoder.klisp.compiler.types

import com.statelesscoder.klisp.compiler.expressions.ExpressionPart


enum class KeywordType {
    LET,
    IF,
    FUN_BANG,
    FUN,
}
data class Keyword(val kwdType: KeywordType) : ExpressionPart()