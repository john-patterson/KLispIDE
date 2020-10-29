package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart
import com.statelesscoder.klisp.compiler.types.KLValue
import com.statelesscoder.klisp.compiler.types.RealizedList

class UserDefinedFunction(val name: Symbol,
                          private val params: List<Symbol>,
                          private val body: ExpressionPart
) : Function() {
    constructor(name: String, params: List<Symbol>, body: ExpressionPart)
        : this(Symbol(name), params, body)

    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLValue {
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

    override fun equals(other: Any?): Boolean {
        return if (other is UserDefinedFunction) {
            this.name == other.name
        } else {
            false
        }
    }

    override fun toString(): String {
        val paramString = params.joinToString(separator = " ") { it.toString() }
        val bodyString = body.toString()
        return "(fun $name [$paramString] $bodyString)"
    }
}