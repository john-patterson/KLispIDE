package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.exceptions.ParsingException
import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.types.KLBool
import com.statelesscoder.klisp.compiler.types.KLValue
import com.statelesscoder.klisp.compiler.types.Keyword
import com.statelesscoder.klisp.compiler.types.KeywordType


class IfExpression(head: ExpressionPart, tail: List<ExpressionPart>) : KeywordExpression(head, tail) {
    private val predicate: ExpressionPart = tail[0]
    private val truePart: ExpressionPart = tail[1]
    private val falsePart: ExpressionPart = tail[2]

    constructor(predicate: ExpressionPart, truePart: ExpressionPart, falsePart: ExpressionPart)
            : this(Keyword(KeywordType.IF), listOf(predicate, truePart, falsePart))

    override fun validate(head: ExpressionPart, tail: List<ExpressionPart>) {
        if (head is Keyword
            && head.kwdType == KeywordType.IF
            && tail.size != 3) {
            throw ParsingException("Encountered IF with less than 3 parts: $tail.")
        }
    }

    override fun execute(executor: Executor, scope: Scope): KLValue {
        return when (val realizedPredicate = executor.realizePart(predicate, scope)) {
            is KLBool -> when (realizedPredicate.truth) {
                true -> executor.execute(truePart, scope)
                false -> executor.execute(falsePart, scope)
            }
            else -> throw RuntimeException("Expected predicate of the if-expression '$this' to evaluate to a boolean.")
        }
    }
}