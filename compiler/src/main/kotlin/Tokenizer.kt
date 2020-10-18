package com.statelesscoder.klisp.compiler

import kotlin.collections.List

enum class TokenType {
    RIGHT_PARENS,
    LEFT_PARENS,
    NUMERIC,
    IDENTIFIER,
    LET,
    IF,
}

data class Token(val text: String, val type: TokenType)

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
                    tokens.add(Token("(", TokenType.LEFT_PARENS))
                    pos += 1
                }
                source[pos] == ')' -> {
                    tokens.add(Token(")", TokenType.RIGHT_PARENS))
                    pos += 1
                }
                else -> {
                    var endpos = pos + 1
                    while (endpos < source.length && !source[endpos].isWhitespace()) {
                        endpos += 1
                    }
                    val part = source.substring(pos, endpos)
                    tokens.add(classifyPart(part))
                    pos = endpos
                }
            }
        }

        return tokens
    }

    private fun classifyPart(text: String): Token {
        return when {
            text.toIntOrNull() != null -> {
                Token(text, TokenType.NUMERIC)
            }
            "if".equals(text, true) -> {
                Token(text, TokenType.IF)
            }
            "let".equals(text, true) -> {
                Token(text, TokenType.LET)
            }
            else -> {
                Token(text, TokenType.IDENTIFIER)
            }
        }
    }
}