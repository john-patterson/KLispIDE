package com.statelesscoder.klisp.compiler.types

import com.statelesscoder.klisp.compiler.Symbol
import com.statelesscoder.klisp.compiler.expressions.Expression

abstract class ExpressionPart

enum class KeywordType {
    LET,
    IF,
    FUN
}

data class Keyword(val kwdType: KeywordType) : ExpressionPart()

enum class ExpressionPartType {
    SYMBOL,
    NUMBER,
    STRING,
    BOOLEAN,
    EXPRESSION,
    KEYWORD,
    LIST,
}
