package com.statelesscoder.klisp.compiler.types

class ExpressionPart(val type: ExpressionPartType) {
    var value: Float? = null
    var truth: Boolean? = null
    var name: String? = null
    var innerText: String? = null
    var keywordType: KeywordType? = null
    var expression: Expression? = null
    var list: KList? = null

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

fun keywordPart(type: KeywordType): ExpressionPart {
    val p = ExpressionPart(ExpressionPartType.KEYWORD)
    p.keywordType = type
    return p
}
fun symbolPart(name: String): ExpressionPart {
    val p = ExpressionPart(ExpressionPartType.SYMBOL)
    p.name = name
    return p
}
fun numericPart(value: Float): ExpressionPart {
    val p = ExpressionPart(ExpressionPartType.NUMBER)
    p.value = value
    return p
}
fun booleanPart(truth: Boolean): ExpressionPart {
    val p = ExpressionPart(ExpressionPartType.BOOLEAN)
    p.truth = truth
    return p
}
fun expressionPart(expr: Expression): ExpressionPart {
    val p = ExpressionPart(ExpressionPartType.EXPRESSION)
    p.expression = expr
    return p
}
fun stringPart(s: String): ExpressionPart {
    val p = ExpressionPart(ExpressionPartType.STRING)
    p.innerText = s
    return p
}
fun listPart(list: KList): ExpressionPart {
    val p = ExpressionPart(ExpressionPartType.LIST)
    p.list = list
    return p
}
