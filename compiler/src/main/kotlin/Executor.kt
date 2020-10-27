package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.types.*

fun runCode(code: String): List<ExecutionResult> {
    val tokenizer = Tokenizer()
    val parser = Parser()
    val executor = Executor()
    val scope = Scope()

    val results = mutableListOf<ExecutionResult>()
    val tokens = tokenizer.scan(code)
    val expressions = parser.parse(tokens)
    for (expression in expressions) {
        val result = ExecutionResult(
            expression,
            executor.execute(expression, scope),
            scope
        )
        results.add(result)
    }

    return results
}

class Executor {
    fun execute(part: ExpressionPart, env: Scope = Scope()): Data = realizePart(part, env)
    fun execute(expr: Expression, env: Scope = Scope()): Data {
        if (expr.head.type == ExpressionPartType.SYMBOL && builtinFunctions.contains(expr.head.name?.toLowerCase())) {
            return handleBuiltinFunction(expr, env)
        } else if (expr.head.type == ExpressionPartType.KEYWORD) {
            return handleKeyword(expr, env)
        }

        val headResult = realizePart(expr.head, env)
        if (headResult.type != DataType.FUNCTION) {
            throw RuntimeException("Attempted to invoke a non-function: ${expr.head}.")
        }

        val argsResults = expr.tail.map { Pair(it, realizePart(it, env)) }
        val argsData = argsResults.map { it.second }
        return headResult.functionValue!!.run(argsData, env)
    }

    private val builtinFunctions: Set<String> = setOf("+", "-", "/", "*", "print")
    private fun handleBuiltinFunction(expr: Expression, scope: Scope): Data {
        val args = expr.tail.map { execute(it, scope) }
        val functionName = expr.head.name?.toLowerCase()

        if (functionName == "print") {
            if (!args.all { it.stringValue != null }) {
                throw RuntimeException("Only strings are printable.")
            }

            val s = args.map { it.stringValue!! }.reduce {acc, s -> "$acc $s" }
            print(s)
            return createData(s)
        }

        if (!args.all { it.numericValue != null }) {
            throw RuntimeException("Only numeric types are compatible with *, +, /, and -.")
        }
        val argsAsNums = args.map { it.numericValue!! }
        return createData(when (functionName) {
            "*" -> argsAsNums.reduce { acc, number -> acc * number }
            "+" -> argsAsNums.reduce { acc, number -> acc + number }
            "-" -> argsAsNums.reduce { acc, number -> acc - number }
            "/" -> argsAsNums.reduce { acc, number -> acc / number }
            else -> throw RuntimeException("$functionName is not a built-in function.")
        })
    }

    private fun handleKeyword(expr: Expression, scope: Scope): Data {
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

                val data = createData(f)
                scope.add(funName, data)
                data
            }
            KeywordType.IF -> {
                val boolPart = expr.tail[0]
                val ifTruePart = expr.tail[1]
                val ifFalsePart = if (expr.tail.size == 3) expr.tail[2] else null

                val boolResult = realizePart(boolPart, scope)

                return if (boolResult.truthyValue == null) {
                    throw RuntimeException("Boolean conditions in if-statements must be truthy: $boolPart.")
                } else if (boolResult.truthyValue!!) {
                    execute(ifTruePart, scope)
                } else if (!boolResult.truthyValue!! && ifFalsePart != null) {
                    execute(ifFalsePart, scope)
                } else {
                    createData(false)
                }
            }
        }
    }

    private fun handleLet(bindings: ExpressionPart, body: ExpressionPart, scope: Scope): Data {
        val newScope = Scope(scope)
        val unitedBindings = listOf(bindings.expression!!.head) + bindings.expression!!.tail
        unitedBindings
            .forEach {
                val symbol = it.expression!!.head.name!!
                val value = execute(it.expression!!.tail[0], newScope)
                newScope.add(symbol, value)
            }

        return execute(body, newScope)
    }

    fun realizePart(arg: ExpressionPart, env: Scope): Data {
        return when (arg.type) {
            ExpressionPartType.STRING -> createData(arg.innerText!!)
            ExpressionPartType.BOOLEAN -> createData(arg.truth!!)
            ExpressionPartType.NUMBER -> createData(arg.value!!)
            ExpressionPartType.SYMBOL -> handleSymbol(arg.name!!, env)
            ExpressionPartType.KEYWORD ->
                throw RuntimeException("Encountered free keyword ${arg.keywordType} in the body of an expression")
            ExpressionPartType.EXPRESSION -> execute(arg.expression!!, env)
            ExpressionPartType.LIST -> {
                arg.list!!.realize(this, env)
                return createData(arg.list!!)
            }
        }
    }

    private fun handleSymbol(symbol: String, env: Scope): Data {
        return env.lookup(symbol)
    }
}



