package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.ParsingException
import com.statelesscoder.klisp.compiler.expressions.*
import com.statelesscoder.klisp.compiler.types.*

class Parser {
    fun parse(tokens: List<Token>): List<Expression> {
        var start = 0
        var end: Int
        var balance = 0

        val collection = mutableListOf<Expression>()
        for (i in tokens.indices) {
            if (balance == 0 && tokens[i].type == TokenType.LEFT_PARENS) {
                start = i
                balance += 1
            } else if (tokens[i].type == TokenType.LEFT_PARENS) {
                balance += 1
            } else if (balance == 1 && tokens[i].type == TokenType.RIGHT_PARENS) {
                end = i
                balance -= 1
                collection.add(parseSingleExpression(tokens.subList(start, end + 1)))
            } else if (tokens[i].type == TokenType.RIGHT_PARENS) {
                balance -= 1
            }
        }

        return collection
    }

    private fun parseSingleList(tokens: List<Token>): List<ExpressionPart> {
        val list = mutableListOf<ExpressionPart>()
        var pos = 1
        while (pos < tokens.size - 1) {
            val part = when (tokens[pos].type) {
                TokenType.LEFT_BRACKET -> {
                    val (end, ls) = parseListPart(tokens, pos)
                    pos = end + 1
                    ls
                }
                TokenType.RIGHT_PARENS -> {
                    val (end, expr) = parseExprPart(tokens, pos)
                    pos = end + 1
                    expr
                }
                else -> {
                    val p = parseSimplePart(tokens[pos])
                    pos += 1
                    p
                }
            }
            list.add(part)
        }
        return list
    }

    fun parseSingleExpression(tokens: List<Token>): Expression {
        if (tokens.isEmpty()) {
            throw ParsingException("Expected start of expression, but got nothing.")
        }

        val tail = mutableListOf<ExpressionPart>()
        assertTokenTypeIsOneOf(tokens[0], TokenType.LEFT_PARENS)
        var i = 2
        val head = if(tokens[1].type == TokenType.LEFT_PARENS) {
            val (end, expr) = parseExprPart(tokens, 1)
            i = end + 1
            expr
        }  else {
            assertTokenTypeIsOneOf(tokens[1],
                TokenType.IDENTIFIER,
                TokenType.LET,
                TokenType.IF,
                TokenType.FUN
            )
            parseSimplePart(tokens[1])
        }

        while (i < (tokens.size - 1)) {
            when (tokens[i].type) {
                TokenType.LEFT_PARENS -> {
                    val (end, expr) = parseExprPart(tokens, i)
                    tail.add(expr)
                    i = end + 1
                }
                TokenType.LEFT_BRACKET -> {
                    val (end, expr) = parseListPart(tokens, i)
                    tail.add(expr)
                    i = end + 1
                }
                else -> {
                    assertTokenTypeIsOneOf(tokens[i],
                        TokenType.IDENTIFIER,
                        TokenType.NUMERIC,
                        TokenType.BOOLEAN,
                        TokenType.STRING)
                    tail.add(parseSimplePart(tokens[i]))
                    i +=1
                }
            }
        }
        assertTokenTypeIsOneOf(tokens[tokens.size - 1], TokenType.RIGHT_PARENS)
        return enrichExpression(head, tail)
    }

    private fun enrichExpression(head: ExpressionPart, tail: List<ExpressionPart>): Expression {
        return when (head) {
            is Keyword -> when (head.kwdType) {
                KeywordType.IF -> IfExpression(head, tail)
                KeywordType.LET -> LetBinding(head, tail)
                KeywordType.FUN -> FunctionDefinition(head, tail)
            }
            else -> Expression(head, tail)
        }
    }

    private fun parseExprPart(tokens: List<Token>, start: Int): Pair<Int, ExpressionPart> {
        val end = findMatchingEnd(tokens, start, TokenType.LEFT_PARENS, TokenType.RIGHT_PARENS)
        val expr = parseSingleExpression(tokens.subList(start, end + 1))
        return Pair(end, expr)
    }

    private fun parseListPart(tokens: List<Token>, start: Int): Pair<Int, ExpressionPart> {
        val end = findMatchingEnd(tokens, start, TokenType.LEFT_BRACKET, TokenType.RIGHT_BRACKET)
        val list = UnrealizedList(parseSingleList(tokens.subList(start, end + 1)))
        return Pair(end, list)
    }

    private fun parseSimplePart(token: Token): ExpressionPart {
        return when (token.type) {
            TokenType.NUMERIC -> KLNumber(token.text.toFloat())
            TokenType.STRING -> KLString(token.text.substring(1, token.text.length - 1))
            TokenType.IDENTIFIER -> Symbol(token.text)
            TokenType.BOOLEAN -> KLBool(token.text.toBoolean())
            TokenType.LET -> Keyword(KeywordType.LET)
            TokenType.IF -> Keyword(KeywordType.IF)
            TokenType.FUN -> Keyword(KeywordType.FUN)
            TokenType.RIGHT_BRACKET -> throw ParsingException("Unexpected ).")
            TokenType.LEFT_BRACKET -> throw ParsingException("Unexpected (.")
            TokenType.RIGHT_PARENS -> throw ParsingException("Unexpected ].")
            TokenType.LEFT_PARENS -> throw ParsingException("Unexpected (.")
        }
    }

    private fun findMatchingEnd(tokens: List<Token>, start: Int, startType: TokenType, endType: TokenType): Int {
        var balance = 1
        var pos = start + 1
        while (pos < tokens.size && balance != 0) {
            if (tokens[pos].type == startType) {
                balance += 1
            } else if (tokens[pos].type == endType) {
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