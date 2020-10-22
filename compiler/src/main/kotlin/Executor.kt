package com.statelesscoder.klisp.compiler


class Executor {
    fun execute(expr: Expression, env: Scope = Scope()): ExecutionResult {
        val headResult = realizePart(expr.head, env)
        if (!headResult.successful || headResult.data.type != DataType.FUNCTION) {
            throw RuntimeException("Attempted to invoke a non-function: ${expr.head}.")
        }

        val argsResults = expr.tail.map { Pair(it, realizePart(it, env)) }
        for ((arg, result) in argsResults) {
            if (!result.successful) {
                throw RuntimeException("Could not resolve argument $arg.")
            }
        }

        val argsData = argsResults.map { it.second.data }
        val finalResult = headResult.innerFunction!!.run(argsData, env)
        return dataToResult(finalResult)
    }

    fun realizePart(arg: ExpressionPart, env: Scope): ExecutionResult {
        return when (arg.type) {
            ExpressionPartType.STRING -> literalResult(arg.innerText!!)
            ExpressionPartType.BOOLEAN -> literalResult(arg.truth!!)
            ExpressionPartType.NUMBER -> literalResult(arg.value!!)
            ExpressionPartType.SYMBOL -> handleSymbol(arg.name!!, env)
            ExpressionPartType.KEYWORD ->
                throw RuntimeException("Encountered free keyword ${arg.keywordType} in the body of an expression")
            ExpressionPartType.EXPRESSION -> execute(arg.expression!!, env)
        }
    }

    private fun handleSymbol(symbol: String, env: Scope): ExecutionResult {
        val data = env.lookup(symbol)
        return when (data.type) {
            DataType.BOOLEAN -> literalResult(data.truthyValue!!)
            DataType.STRING -> literalResult(data.stringValue!!)
            DataType.NUMBER -> literalResult(data.numericValue!!)
            DataType.FUNCTION -> literalResult(data.functionValue!!)
        }
    }
}

fun literalResult(s: String): ExecutionResult = ExecutionResult(true, stringData(s))
fun literalResult(b: Boolean): ExecutionResult = ExecutionResult(true, truthyData(b))
fun literalResult(n: Number): ExecutionResult = ExecutionResult(true, numericData(n))
fun literalResult(f: Function): ExecutionResult = ExecutionResult(true, functionData(f))
fun dataToResult(d: Data): ExecutionResult = when (d.type) {
    DataType.NUMBER -> literalResult(d.numericValue!!)
    DataType.STRING -> literalResult(d.stringValue!!)
    DataType.BOOLEAN -> literalResult(d.truthyValue!!)
    DataType.FUNCTION -> literalResult(d.functionValue!!)
}

// TODO: This is a redundant type
class ExecutionResult(val successful: Boolean, val data: Data) {
    var innerText: String? = data.stringValue
    var innerValue: Number? = data.numericValue
    var innerTruth: Boolean? = data.truthyValue
    var innerFunction: Function? = data.functionValue
}




