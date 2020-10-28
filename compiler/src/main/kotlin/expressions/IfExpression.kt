package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.types.*

data class IfExpression(val predicate: ExpressionPart, val truePart: ExpressionPart, val falsePart: ExpressionPart)
    : Expression(Keyword(KeywordType.IF), listOf(predicate, truePart, falsePart)) {
    override fun execute(executor: Executor, scope: Scope): Data {
        val realizedPredicate = executor.realizePart(predicate, scope)
        return when (realizedPredicate.truthyValue) {
            true -> executor.execute(truePart, scope)
            false -> executor.execute(falsePart, scope)
            null -> throw RuntimeException("Expected predicate of the if-expression '$this' to evaluate to a boolean.")
        }
    }
}