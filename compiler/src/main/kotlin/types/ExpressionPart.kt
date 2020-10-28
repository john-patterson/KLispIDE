package com.statelesscoder.klisp.compiler.types

import com.statelesscoder.klisp.compiler.Symbol

open class ExpressionPart {
    val type: ExpressionPartType;
    var value: Float? = null
    var truth: Boolean? = null
    var name: String? = null
    var innerText: String? = null
    var keywordType: KeywordType? = null
    var expression: Expression? = null
    var list: KList? = null

    constructor(value: Float)
        : this(ExpressionPartType.NUMBER) {
            this.value = value
    }

    constructor(value: Boolean)
            : this(ExpressionPartType.BOOLEAN) {
        this.truth = value
    }

    constructor(value: String)
        : this(ExpressionPartType.STRING) {
            this.innerText = value
    }

    constructor(value: Expression)
            : this(ExpressionPartType.EXPRESSION) {
        this.expression = value
    }

    constructor(value: KList)
            : this(ExpressionPartType.LIST) {
        this.list = value
    }

    constructor(value: KeywordType)
            : this(ExpressionPartType.KEYWORD) {
        this.keywordType = value
    }

    constructor(value: Symbol)
            : this(ExpressionPartType.SYMBOL) {
        this.name = value.symbolName
    }

    constructor(type: ExpressionPartType)
    {
        this.type = type
    }

    override fun toString(): String = when(type) {
        ExpressionPartType.NUMBER -> value.toString()
        ExpressionPartType.SYMBOL -> name.toString()
        ExpressionPartType.BOOLEAN -> if (truth!!) "true" else "false"
        ExpressionPartType.KEYWORD -> keywordType.toString()
        ExpressionPartType.STRING -> "\"$innerText\""
        ExpressionPartType.EXPRESSION -> expression.toString()
        ExpressionPartType.LIST -> list.toString()

    }
}

enum class KeywordType {
    LET,
    IF,
    FUN
}

enum class ExpressionPartType {
    SYMBOL,
    NUMBER,
    STRING,
    BOOLEAN,
    EXPRESSION,
    KEYWORD,
    LIST,
}
