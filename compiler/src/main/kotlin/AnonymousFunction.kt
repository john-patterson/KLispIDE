package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart
import com.statelesscoder.klisp.compiler.types.KLValue
import com.statelesscoder.klisp.compiler.types.RealizedList

class AnonymousFunction(private val params: List<Symbol>, private val body: ExpressionPart, private val definingScope: Scope): Function() {
    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLValue {
        if (args.items.size != params.size) {
            throw RuntimeException("Function '$this' expects '${params.size}' arguments, but got '${args.items.size}'.")
        }

        val boundScope = Scope(scope, definingScope)
        for (i in args.items.indices) {
            val symbolPart = params[i]
            boundScope.add(symbolPart, args.items[i])
        }

        return executor.realizePart(body, boundScope)
    }

    override fun toString(): String {
        val paramString = params.joinToString(separator = " ") { it.toString() }
        val bodyString = body.toString()
        return "(fun [$paramString] $bodyString)"
    }
}