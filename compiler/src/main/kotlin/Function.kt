package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.expressions.Expression
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart
import com.statelesscoder.klisp.compiler.types.*

class Function(private val executor: Executor,
               val name: Symbol,
               private val params: List<Symbol>,
               private val body: ExpressionPart
): Expression(name, listOf(UnrealizedList(params), body)) {
    constructor(executor: Executor, name: String, params: List<Symbol>, body: ExpressionPart)
        : this(executor, Symbol(name), params, body)

    fun run(args: RealizedList, scope: Scope = Scope()): KLValue {
        if (args.items.size != params.size) {
            throw RuntimeException("Function '$name' expects '${params.size}' arguments, but got '${args.items.size}'.")
        }

        val boundScope = Scope(scope)
        for (i in args.items.indices) {
            val symbolPart = params[i]
            boundScope.add(symbolPart, args.items[i])
        }

        return executor.realizePart(body, boundScope)
    }

    override fun toString(): String {
        val paramString = params.joinToString(separator = " ") { it.toString() }
        val bodyString = body.toString()
        return "(fun $name [$paramString] $bodyString)"
    }
}