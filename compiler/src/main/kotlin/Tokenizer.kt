package com.statelesscoder.klisp.compiler

import java.text.FieldPosition
import kotlin.collections.List

enum class TokenType {
    RIGHT_PARENS,
    LEFT_PARENS,
    NUMERIC,
    BOOLEAN,
    IDENTIFIER,
    LET,
    IF,
    FUN,
}

data class Token(val text: String, val type: TokenType, val pos: Int)

fun identifierToken(text: String, pos: Int) =
    Token(text, TokenType.IDENTIFIER, pos)
fun numericToken(text: String, pos: Int) =
    Token(text, TokenType.NUMERIC, pos)
fun booleanToken(text: String, pos: Int) =
    Token(text, TokenType.BOOLEAN, pos)
fun leftParensToken(pos: Int) =
    Token("(", TokenType.LEFT_PARENS, pos)
fun rightParensToken(pos: Int) =
    Token(")", TokenType.RIGHT_PARENS, pos)

class Tokenizer {
    fun scan(source: String): List<Token> {
        var tokens = mutableListOf<Token>()
        var pos = 0

        while (pos < source.length) {
            when {
                source[pos].isWhitespace() -> {
                    while (pos < source.length && source[pos].isWhitespace()) {
                        pos += 1
                    }
                }
                source[pos] == '(' -> {
                    tokens.add(Token("(", TokenType.LEFT_PARENS, pos))
                    pos += 1
                }
                source[pos] == ')' -> {
                    tokens.add(Token(")", TokenType.RIGHT_PARENS, pos))
                    pos += 1
                }
                else -> {
                    var endpos = pos + 1
                    while (endpos < source.length
                        && !source[endpos].isWhitespace()
                        && source[endpos] != ')') {
                        endpos += 1
                    }
                    val part = source.substring(pos, endpos)
                    tokens.add(classifyPart(part, pos))
                    pos = endpos
                }
            }
        }

        return tokens
    }

    private fun classifyPart(text: String, pos: Int): Token {
        return when {
            text.toFloatOrNull() != null -> numericToken(text, pos)
            "if".equals(text, true) ->
                Token(text, TokenType.IF, pos)
            "let".equals(text, true) ->
                Token(text, TokenType.LET, pos)
            "fun".equals(text, true) ->
                Token(text, TokenType.FUN, pos)
            "true".equals(text, true) ->
                booleanToken(text, pos)
            "false".equals(text, true) ->
                booleanToken(text, pos)
            else -> identifierToken(text, pos)
        }
    }
}