package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.Symbol
import com.statelesscoder.klisp.compiler.exceptions.ParsingException
import com.statelesscoder.klisp.compiler.types.*

class LetBinding(head: ExpressionPart, tail: List<ExpressionPart>):  KeywordExpression(head, tail) {
    private val bindings: Expression = tail[0] as Expression
    private val body: ExpressionPart = tail[1]

    constructor(bindings: Expression, body: ExpressionPart)
            : this(Keyword(KeywordType.IF), listOf(bindings, body))

    override fun validate(head: ExpressionPart, tail: List<ExpressionPart>) {
        if (head is Keyword
            && head.kwdType == KeywordType.LET
            && tail.size != 2
            && tail.first() !is Expression) {
            throw ParsingException("Encountered IF with less than 3 parts: $tail.")
        }
    }

    override fun execute(executor: Executor, scope: Scope): KLValue {
        val realizedBindings = (listOf(bindings.head) + bindings.tail)
            .map { it as Expression }
            .map { Pair(it.head as Symbol, it.tail[0]) }
            .map { Pair(it.first, executor.realizePart(it.second, scope)) }
        val newScope = Scope(scope)

        for (part in realizedBindings) {
            newScope.add(part.first, part.second)
        }

        return executor.realizePart(body, newScope)
    }
}