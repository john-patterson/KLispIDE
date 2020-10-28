package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.expressions.Expression
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart
import com.statelesscoder.klisp.compiler.types.*

class Function(private val executor: Executor,
               val name: Symbol,
               private val params: KList,
               private val body: ExpressionPart
): Expression(name, listOf(params, body)) {
    constructor(executor: Executor, name: String, params: List<ExpressionPart>, body: ExpressionPart)
        : this(executor, Symbol(name), KList(params), body)

    fun run(args: List<Data>, scope: Scope = Scope()): Data {
        if (args.size != params.unrealizedItems.size) {
            throw RuntimeException("Function '$name' expects '${params.unrealizedItems.size}' arguments, but got '${args.size}'.")
        }

        val boundScope = Scope(scope)
        for (i in args.indices) {
            val symbolPart = params.unrealizedItems[i]
            if (symbolPart is Symbol) {
                boundScope.add(symbolPart, args[i])
            } else {
                throw RuntimeException("Encountered non-symbol in function parameter list $symbolPart.")
            }
        }

        return executor.realizePart(body, boundScope)
    }

    override fun toString(): String {
        val paramString = params.unrealizedItems.joinToString(separator = " ") { it.toString() }
        val bodyString = body.toString()
        return "(fun $name [$paramString] $bodyString)"
    }
}