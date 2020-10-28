package com.statelesscoder.klisp.compiler

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