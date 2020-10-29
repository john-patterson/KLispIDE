package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.types.KLValue

abstract class KeywordExpression : Expression {
    constructor(head: ExpressionPart, tail: List<ExpressionPart>)
        : super(head, tail) {
        validate(head, tail)
    }
    abstract fun validate(head: ExpressionPart, tail: List<ExpressionPart>)
    abstract override fun execute(executor: Executor, scope: Scope): KLValue
}