package com.statelesscoder.klisp.compiler

import tornadofx.times


class Executor {
    fun execute(part: ExpressionPart, env: Scope = Scope()): ExecutionResult = realizePart(part, env)
    fun execute(expr: Expression, env: Scope = Scope()): ExecutionResult {
        if (expr.head.type == ExpressionPartType.SYMBOL && builtinFunctions.contains(expr.head.name?.toLowerCase())) {
            return handleBuiltinFunction(expr, env)
        } else if (expr.head.type == ExpressionPartType.KEYWORD) {
            return handleKeyword(expr, env)
        }

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

    private val builtinFunctions: Set<String> = setOf("+", "-", "/", "*", "print")
    private fun handleBuiltinFunction(expr: Expression, scope: Scope): ExecutionResult {
        val args = expr.tail.map { execute(it, scope) }
        val functionName = expr.head.name?.toLowerCase()

        if (functionName == "print") {
            if (!args.all { it.innerText != null }) {
                throw RuntimeException("Only strings are printable.")
            }

            val s = args.map { it.innerText!! }.reduce {acc, s -> "$acc $s" }
            print(s)
            return literalResult(s)
        }

        if (!args.all { it.innerValue != null }) {
            throw RuntimeException("Only numeric types are compatible with *, +, /, and -.")
        }
        val argsAsNums = args.map { it.innerValue!! }
        return literalResult(when (functionName) {
            "*" -> argsAsNums.reduce {acc, number -> acc * number }
            "+" -> argsAsNums.reduce {acc, number -> acc + number }
            "-" -> argsAsNums.reduce {acc, number -> acc - number }
            "/" -> argsAsNums.reduce {acc, number -> acc / number }
            else -> throw RuntimeException("$functionName is not a built-in function.")
        })
    }

    private fun handleKeyword(expr: Expression, scope: Scope): ExecutionResult {
        return when (expr.head.keywordType!!) {
            KeywordType.LET -> {
                val bindings = expr.tail[0]
                val body = expr.tail[1]
                return handleLet(bindings, body, scope)
            }
            KeywordType.FUN -> {
                val funName = expr.tail[0].name!!
                val f = if (expr.tail.size == 3) {
                    val params = listOf(expr.tail[1].expression!!.head) + expr.tail[1].expression!!.tail
                    val body = expr.tail[2]
                    Function(this, funName, params, body)
                } else {
                    Function(this, funName, emptyList(), expr.tail[1])
                }

                scope.add(funName, functionData(f))
                literalResult(f)
            }
            KeywordType.IF -> {
                val boolPart = expr.tail[0]
                val ifTruePart = expr.tail[1]
                val ifFalsePart = if (expr.tail.size == 3) expr.tail[2] else null

                val boolResult = realizePart(boolPart, scope)

                return if (boolResult.innerTruth == null) {
                    throw RuntimeException("Boolean conditions in if-statements must be truthy: $boolPart.")
                } else if (boolResult.innerTruth!!) {
                    execute(ifTruePart, scope)
                } else if (!boolResult.innerTruth!! && ifFalsePart != null) {
                    execute(ifFalsePart, scope)
                } else {
                    literalResult(false)
                }
            }
        }
    }

    private fun handleLet(bindings: ExpressionPart, body: ExpressionPart, scope: Scope): ExecutionResult {
        val newScope = Scope(scope)
        val unitedBindings = listOf(bindings.expression!!.head) + bindings.expression!!.tail
        unitedBindings
            .forEach {
                val symbol = it.expression!!.head.name!!
                val value = execute(it.expression!!.tail[0], newScope)
                newScope.add(symbol, value.data)
            }

        return execute(body, newScope)
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
fun literalResult(n: Float): ExecutionResult = ExecutionResult(true, numericData(n))
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
    var innerValue: Float? = data.numericValue
    var innerTruth: Boolean? = data.truthyValue
    var innerFunction: Function? = data.functionValue
}




