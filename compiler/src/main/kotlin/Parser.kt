package com.statelesscoder.klisp.compiler


enum class KeywordType {
    LET,
    IF,
    FUN
}

enum class ExpressionPartType {
    SYMBOL,
    NUMBER,
    BOOLEAN,
    EXPRESSION,
    KEYWORD,
}
class ExpressionPart(val type: ExpressionPartType) {
    var value: Number? = null
    var truth: Boolean? = null
    var name: String? = null
    var keywordType: KeywordType? = null
    var expression: Expression? = null
}
data class Expression(val head: ExpressionPart, val tail: List<ExpressionPart>)

class ParsingException(message:String): Exception(message)

class Parser() {
    fun parse(tokens: List<Token>): Expression {
        if (tokens.isEmpty()) {
            throw ParsingException("Expected start of expression, but got nothing.")
        }

        var tail = mutableListOf<ExpressionPart>()
        assertTokenTypeIsOneOf(tokens[0], TokenType.LEFT_PARENS)
        var i = 2
        val head = if(tokens[1].type == TokenType.LEFT_PARENS) {
            val (end, expr) = parseExprPart(tokens, 1)
            i = end + 1
            expr
        } else {
            assertTokenTypeIsOneOf(tokens[1], TokenType.IDENTIFIER, TokenType.LET, TokenType.IF)
            parsePart(tokens[1])
        }

        while (i < (tokens.size - 1)) {
            if (tokens[i].type == TokenType.LEFT_PARENS) {
                val (end, expr) = parseExprPart(tokens, i)
                tail.add(expr)
                i = end + 1
            } else {
                assertTokenTypeIsOneOf(tokens[i], TokenType.IDENTIFIER, TokenType.NUMERIC, TokenType.BOOLEAN)
                tail.add(parsePart(tokens[i]))
                i +=1
            }
        }
        assertTokenTypeIsOneOf(tokens[tokens.size - 1], TokenType.RIGHT_PARENS)

        if (head.type == ExpressionPartType.KEYWORD
            && head.keywordType == KeywordType.IF
            && tail.size != 3) {
            throw ParsingException("Encountered IF with less than 3 parts: $tail.")
        }
        return Expression(head, tail)
    }

    private fun parseExprPart(tokens: List<Token>, start: Int): Pair<Int, ExpressionPart> {
        val end = findExpressionEnd(tokens, start)
        val expr = ExpressionPart(ExpressionPartType.EXPRESSION)
        expr.expression = parse(tokens.subList(start, end + 1))
        return Pair(end, expr)
    }

    private fun parsePart(token: Token): ExpressionPart {
        return when (token.type) {
            TokenType.NUMERIC -> {
                val part = ExpressionPart(ExpressionPartType.NUMBER)
                part.value = token.text.toFloat()
                part
            }
            TokenType.IDENTIFIER -> {
                val part = ExpressionPart(ExpressionPartType.SYMBOL)
                part.name = token.text
                part
            }
            TokenType.BOOLEAN -> {
                val part = ExpressionPart(ExpressionPartType.BOOLEAN)
                part.truth = token.text.toBoolean()
                part
            }
            TokenType.LET -> {
                val part = ExpressionPart(ExpressionPartType.KEYWORD)
                part.keywordType = KeywordType.LET
                part
            }
            TokenType.IF -> {
                val part = ExpressionPart(ExpressionPartType.KEYWORD)
                part.keywordType = KeywordType.IF
                part
            }
            else -> throw ParsingException("Unexpected token '${token.text}' of type ${token.type}.")
        }
    }


    fun findExpressionEnd(tokens: List<Token>, start: Int): Int {
        var balance = 1
        var pos = start + 1
        while (pos < tokens.size && balance != 0) {
            if (tokens[pos].type == TokenType.LEFT_PARENS) {
                balance += 1
            } else if (tokens[pos].type == TokenType.RIGHT_PARENS) {
                balance -= 1
            }
            pos += 1
        }

        return pos - 1
    }

    private fun assertTokenTypeIsOneOf(got: Token, vararg types: TokenType) {
        if (!types.any { it == got.type }) {
            val sPart = if (types.size > 1) "s" else ""
            val expectedString = types.joinToString("', '") { it.toString() }
            throw ParsingException("Expected token of type${sPart}: '${expectedString}', but encountered incompatible '${got.text}'.")
        }
    }
}