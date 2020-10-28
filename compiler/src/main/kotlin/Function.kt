package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.types.*

class Function(private val executor: Executor,
               val name: String,
               private val params: KList,
               private val body: ExpressionPart
): Expression(symbolPart(name), listOf(ExpressionPart(params), body)) {
    constructor(executor: Executor, name: String, params: List<ExpressionPart>, body: ExpressionPart)
        : this(executor, name, KList(params), body)

    fun run(args: List<Data>, scope: Scope = Scope()): Data {
        if (args.size != params.unrealizedItems.size) {
            throw RuntimeException("Function '$name' expects '${params.unrealizedItems.size}' arguments, but got '${args.size}'.")
        }

        val boundScope = Scope(scope)
        for (i in args.indices) {
            boundScope.add(params.unrealizedItems[i].name!!, args[i])
        }

        return when (body.type) {
            ExpressionPartType.BOOLEAN,
            ExpressionPartType.NUMBER,
            ExpressionPartType.SYMBOL,
            ExpressionPartType.STRING,
            ExpressionPartType.LIST ->
                executor.realizePart(body, boundScope)
            ExpressionPartType.EXPRESSION, ExpressionPartType.KEYWORD ->
                executor.execute(body.expression!!, boundScope)
        }
    }

    override fun toString(): String {
        val paramString = params.unrealizedItems.joinToString(separator = " ") { it.toString() }
        val bodyString = body.toString()
        return "(fun $name [$paramString] $bodyString)"
    }
}