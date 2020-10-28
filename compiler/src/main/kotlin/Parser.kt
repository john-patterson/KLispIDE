package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.ParsingException
import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.types.*

data class LetBinding(val bindings: Expression, val body: ExpressionPart)
    : Expression(keywordPart(KeywordType.LET), listOf(expressionPart(bindings), body)) {
    override fun execute(executor: Executor, scope: Scope): Data {
        val realizedBindings = (listOf(bindings.head) + bindings.tail)
            .map { Pair(it.expression!!.head.name!!, it.expression!!.tail[0]) }
            .map { Pair(it.first, executor.realizePart(it.second, scope)) }
        val newScope = Scope(scope)

        for (part in realizedBindings) {
            newScope.add(part.first, part.second)
        }

        return executor.realizePart(body, newScope)
    }
}

data class IfExpression(val predicate: ExpressionPart, val truePart: ExpressionPart, val falsePart: ExpressionPart)
    : Expression(keywordPart(KeywordType.IF), listOf(predicate, truePart, falsePart)) {
    override fun execute(executor: Executor, scope: Scope): Data {
        val realizedPredicate = executor.realizePart(predicate, scope)
        return when (realizedPredicate.truthyValue) {
            true -> executor.execute(truePart, scope)
            false -> executor.execute(falsePart, scope)
            null -> throw RuntimeException("Expected predicate of the if-expression '$this' to evaluate to a boolean.")
        }
    }
}

class Parser() {
    fun parse(tokens: List<Token>): List<Expression> {
        var start = 0
        var end = 0
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

        var tail = mutableListOf<ExpressionPart>()
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
            if (tokens[i].type == TokenType.LEFT_PARENS) {
                val (end, expr) = parseExprPart(tokens, i)
                tail.add(expr)
                i = end + 1
            } else if (tokens[i].type == TokenType.LEFT_BRACKET) {
                val (end, expr) = parseListPart(tokens, i)
                tail.add(expr)
                i = end + 1
            } else {
                assertTokenTypeIsOneOf(tokens[i],
                    TokenType.IDENTIFIER,
                    TokenType.NUMERIC,
                    TokenType.BOOLEAN,
                    TokenType.STRING)
                tail.add(parseSimplePart(tokens[i]))
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
                val argsValid = tail[1].type == ExpressionPartType.LIST
                        && tail[1].list != null
                        && tail[1].list!!.unrealizedItems.all { it.type == ExpressionPartType.SYMBOL }

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
        val end = findMatchingEnd(tokens, start, TokenType.LEFT_PARENS, TokenType.RIGHT_PARENS)
        val expr = ExpressionPart(ExpressionPartType.EXPRESSION)
        expr.expression = parseSingleExpression(tokens.subList(start, end + 1))
        return Pair(end, expr)
    }

    private fun parseListPart(tokens: List<Token>, start: Int): Pair<Int, ExpressionPart> {
        val end = findMatchingEnd(tokens, start, TokenType.LEFT_BRACKET, TokenType.RIGHT_BRACKET)
        val list = ExpressionPart(ExpressionPartType.LIST)
        list.list = KList(parseSingleList(tokens.subList(start, end + 1)))
        return Pair(end, list)
    }

    private fun parseSimplePart(token: Token): ExpressionPart {
        return when (token.type) {
            TokenType.NUMERIC -> {
                val part =
                    ExpressionPart(ExpressionPartType.NUMBER)
                part.value = token.text.toFloat()
                part
            }
            TokenType.STRING -> {
                val part =
                    ExpressionPart(ExpressionPartType.STRING)
                part.innerText = token.text.substring(1, token.text.length - 1)
                part
            }
            TokenType.IDENTIFIER -> {
                val part =
                    ExpressionPart(ExpressionPartType.SYMBOL)
                part.name = token.text
                part
            }
            TokenType.BOOLEAN -> {
                val part =
                    ExpressionPart(ExpressionPartType.BOOLEAN)
                part.truth = token.text.toBoolean()
                part
            }
            TokenType.LET -> {
                val part =
                    ExpressionPart(ExpressionPartType.KEYWORD)
                part.keywordType = KeywordType.LET
                part
            }
            TokenType.IF -> {
                val part =
                    ExpressionPart(ExpressionPartType.KEYWORD)
                part.keywordType = KeywordType.IF
                part
            }
            TokenType.FUN -> {
                val part =
                    ExpressionPart(ExpressionPartType.KEYWORD)
                part.keywordType = KeywordType.FUN
                part
            }
            else -> throw ParsingException("Unexpected token '${token.text}' of type ${token.type}.")
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