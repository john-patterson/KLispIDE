package com.statelesscoder.klisp.compiler.types

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.exceptions.RuntimeException

data class KList(val unrealizedItems: List<ExpressionPart>) {
    var realizedData: List<Data> = emptyList()
        private set

    fun realize(executor: Executor, scope: Scope) {
        if (realizedData.isEmpty()) {
            realizedData = unrealizedItems.map { executor.realizePart(it, scope) }
        }
    }

    override fun toString(): String {
        if (realizedData.size != unrealizedItems.size) {
            throw RuntimeException("List $this is not realized, cannot be printed.")
        }
        val itemString = realizedData.joinToString(separator = " ") { it.toString() }
        return "[$itemString]"
    }
}