package com.statelesscoder.klisp.compiler


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
}
class ExpressionPart(val type: ExpressionPartType) {
    var value: Number? = null
    var truth: Boolean? = null
    var name: String? = null
    var innerText: String? = null
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
            assertTokenTypeIsOneOf(tokens[1],
                TokenType.IDENTIFIER,
                TokenType.LET,
                TokenType.IF,
                TokenType.FUN
            )
            parsePart(tokens[1])
        }

        while (i < (tokens.size - 1)) {
            if (tokens[i].type == TokenType.LEFT_PARENS) {
                val (end, expr) = parseExprPart(tokens, i)
                tail.add(expr)
                i = end + 1
            } else {
                assertTokenTypeIsOneOf(tokens[i],
                    TokenType.IDENTIFIER,
                    TokenType.NUMERIC,
                    TokenType.BOOLEAN,
                    TokenType.STRING)
                tail.add(parsePart(tokens[i]))
                i +=1
            }
        }
        assertTokenTypeIsOneOf(tokens[tokens.size - 1], TokenType.RIGHT_PARENS)

        validateIfExpr(head, tail)
        validateFunctionDecl(head, tail)
        return Expression(head, tail)
    }

    private fun validateIfExpr(head: ExpressionPart, tail: List<ExpressionPart>) {
        if (head.type == ExpressionPartType.KEYWORD
            && head.keywordType == KeywordType.IF
            && tail.size != 3) {
            throw ParsingException("Encountered IF with less than 3 parts: $tail.")
        }
    }

    private fun validateFunctionDecl(head: ExpressionPart, tail: List<ExpressionPart>) {
        if (head.type == ExpressionPartType.KEYWORD
            && head.keywordType == KeywordType.FUN) {

            if (tail.size == 2 && tail[0].type != ExpressionPartType.SYMBOL) {
                // This is the case without args
                throw ParsingException("Encountered function without symbol as name: $tail")
            } else if (tail.size == 3) { // This has all parts
                // This is the case without args
                val nameValid = tail[0].type == ExpressionPartType.SYMBOL
                val argsValid = tail[1].type == ExpressionPartType.EXPRESSION
                        && tail[1].expression != null
                        && tail[1].expression!!.head.type == ExpressionPartType.SYMBOL
                        && tail[1].expression!!.tail.all { it.type == ExpressionPartType.SYMBOL }

                if (!nameValid) {
                    throw ParsingException("Encountered function without symbol as name: $tail")
                } else if (!argsValid) {
                    throw ParsingException("Encountered function with invalid item in arg list: $tail")
                }
            } else if (tail.size < 2) {
                throw ParsingException("Encountered function with too few parts: $tail")
            } else if (tail.size > 3) {
                throw ParsingException("Encountered function with too many parts: $tail")
            }
        }
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
            TokenType.STRING -> {
                val part = ExpressionPart(ExpressionPartType.STRING)
                part.innerText = token.text.substring(1, token.text.length - 1)
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
            TokenType.FUN -> {
                val part = ExpressionPart(ExpressionPartType.KEYWORD)
                part.keywordType = KeywordType.FUN
                part
            }
            else -> throw ParsingException("Unexpected token '${token.text}' of type ${token.type}.")
        }
    }

    private fun findExpressionEnd(tokens: List<Token>, start: Int): Int {
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