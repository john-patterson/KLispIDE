package com.statelesscoder.klisp.compiler.types


enum class KeywordType {
    LET,
    IF,
    FUN
}
data class Keyword(val kwdType: KeywordType) : ExpressionPart()