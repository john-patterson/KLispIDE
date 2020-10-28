package com.statelesscoder.klisp.compiler.types

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart


data class RealizedList(val items: List<KLValue>) : KLValue() {
    constructor() : this(emptyList())

    override fun equals(other: Any?): Boolean {
        return if (other is RealizedList) {
            this.items == other.items
        } else {
            false
        }
    }

    override fun toString(): String {
        val itemString = items.joinToString(separator = " ") { it.toString() }
        return "[$itemString]"
    }
}

data class UnrealizedList(val items: List<ExpressionPart>) : ExpressionPart() {
    constructor() : this(emptyList())
    fun realize(executor: Executor, scope: Scope): RealizedList {
        return RealizedList(items.map { executor.realizePart(it, scope) })
    }
}

