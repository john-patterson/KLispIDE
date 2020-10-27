package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.ScanningException
import com.statelesscoder.klisp.compiler.types.*

class Tokenizer {
    private val nonIdentifierCharacters = setOf('[', ']', '(', ')', '"')
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
                source[pos] == '[' -> {
                    tokens.add(leftBracketToken(pos))
                    pos += 1
                }
                source[pos] == ']' -> {
                    tokens.add(rightBracketToken(pos))
                    pos += 1
                }
                source[pos] == '(' -> {
                    tokens.add(leftParensToken(pos))
                    pos += 1
                }
                source[pos] == ')' -> {
                    tokens.add(rightParensToken(pos))
                    pos += 1
                }
                source[pos] == '"' -> {
                    var endpos = pos + 1
                    while (endpos < source.length && source[endpos] != '"') {
                        endpos += 1
                    }

                    if (endpos == source.length) {
                        throw ScanningException(
                            "Expected end to string: ${source.substring(
                                pos
                            )}"
                        )
                    }

                    val s = source.substring(pos, endpos + 1)
                    tokens.add(stringToken(s, pos))
                    pos = endpos + 1
                }
                else -> {
                    var endpos = pos + 1
                    while (endpos < source.length
                        && !source[endpos].isWhitespace()
                        && !nonIdentifierCharacters.contains(source[endpos])) {
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
            text.toFloatOrNull() != null -> numericToken(
                text,
                pos
            )
            "if".equals(text, true) ->
                Token(
                    text,
                    TokenType.IF,
                    pos
                )
            "let".equals(text, true) ->
                Token(
                    text,
                    TokenType.LET,
                    pos
                )
            "fun".equals(text, true) ->
                Token(
                    text,
                    TokenType.FUN,
                    pos
                )
            "true".equals(text, true) ->
                booleanToken(text, pos)
            "false".equals(text, true) ->
                booleanToken(text, pos)
            else -> identifierToken(text, pos)
        }
    }
}