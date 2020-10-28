package com.statelesscoder.klisp.compiler.types

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope

open class Expression(val head: ExpressionPart, val tail: List<ExpressionPart>) {
    open fun execute(executor: Executor, scope: Scope): Data {
        return executor.execute(this, scope)
    }

    override fun toString(): String {
        val headString = head.toString()
        val tailString = tail.joinToString(separator = " ") { it.toString() }
        return "($headString $tailString)"
    }
}

