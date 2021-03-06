package com.statelesscoder.klisp.compiler.types

data class Token(val text: String, val type: TokenType, val pos: Int)

enum class TokenType {
    RIGHT_BRACKET,
    LEFT_BRACKET,
    RIGHT_PARENS,
    LEFT_PARENS,
    NUMERIC,
    BOOLEAN,
    IDENTIFIER,
    STRING,
    LET,
    IF,
    FUN_BANG,
    FUN,
}

fun identifierToken(text: String, pos: Int) =
    Token(
        text,
        TokenType.IDENTIFIER,
        pos
    )
fun numericToken(text: String, pos: Int) =
    Token(
        text,
        TokenType.NUMERIC,
        pos
    )
fun stringToken(text: String, pos: Int) =
    Token(
        text,
        TokenType.STRING,
        pos
    )
fun booleanToken(text: String, pos: Int) =
    Token(
        text,
        TokenType.BOOLEAN,
        pos
    )
fun leftParensToken(pos: Int) =
    Token(
        "(",
        TokenType.LEFT_PARENS,
        pos
    )
fun rightParensToken(pos: Int) =
    Token(
        ")",
        TokenType.RIGHT_PARENS,
        pos
    )
fun leftBracketToken(pos: Int) =
    Token(
        "[",
        TokenType.LEFT_BRACKET,
        pos
    )
fun rightBracketToken(pos: Int) =
    Token(
        "]",
        TokenType.RIGHT_BRACKET,
        pos
    )
