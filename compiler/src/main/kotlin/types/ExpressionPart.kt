package com.statelesscoder.klisp.compiler.types

abstract class ExpressionPart

enum class KeywordType {
    LET,
    IF,
    FUN
}

data class Keyword(val kwdType: KeywordType) : ExpressionPart()
