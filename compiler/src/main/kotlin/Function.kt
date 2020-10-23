package com.statelesscoder.klisp.compiler

class Function(private val executor: Executor,
               private val name: String,
               private val params: List<ExpressionPart>,
               private val body: ExpressionPart
) {
    fun run(args: List<Data>, scope: Scope = Scope()): Data {
        if (args.size != params.size) {
            throw RuntimeException("Function '$name' expects '${params.size}' arguments, but got '${args.size}'.")
        }

        val boundScope = Scope(scope)
        for (i in args.indices) {
            boundScope.add(params[i].name!!, args[i])
        }

        return when (body.type) {
            ExpressionPartType.BOOLEAN, ExpressionPartType.NUMBER, ExpressionPartType.SYMBOL, ExpressionPartType.STRING ->
                executor.realizePart(body, boundScope).data
            ExpressionPartType.EXPRESSION, ExpressionPartType.KEYWORD ->
                executor.execute(body.expression!!, boundScope).data
        }
    }
}