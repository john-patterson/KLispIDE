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
        if (headResult.dataType != DataType.FUNCTION) {
            throw RuntimeException("Attempted to invoke a non-function: ${expr.head}.")
        }

        val argsResults = expr.tail.map { Pair(it, realizePart(it, env)) }
        val argsData = argsResults.map { it.second }
        return headResult.functionValue!!.run(argsData, env)
    }

    private val numericBuiltins = setOf("+", "-", "/", "*")
    private val listBuiltins = setOf("car", "cdr", "cons")
    private val equalityBuiltins = setOf("eq", "neq")
    private val logicBuiltins = setOf("and", "or", "not")
    private val builtinFunctions: Set<String> = numericBuiltins
        .plus(listBuiltins)
        .plus(equalityBuiltins)
        .plus(logicBuiltins)
        .plusElement("print")
    private fun handleBuiltinFunction(expr: Expression, scope: Scope): Data {
        val args = expr.tail.map { execute(it, scope) }
        val functionName = expr.head.name?.toLowerCase()

        if (functionName == "print") {
            if (!args.all { it.stringValue != null }) {
                throw RuntimeException("Only strings are printable.")
            }

            val s = args.map { it.stringValue!! }.reduce {acc, s -> "$acc $s" }
            print(s)
            return Data(s)
        }

        if (listBuiltins.contains(functionName)) {
            return handleListBuiltIn(functionName!!, expr, args, scope)
        }

        if (logicBuiltins.contains(functionName)) {
            return handleLogicBuiltIn(functionName!!, args)
        }

        if (equalityBuiltins.contains(functionName)) {
            return handleEqualityBuiltIn(functionName!!, args)
        }

        return handleNumericBuiltIn(functionName!!, args)

    }

    private fun handleListBuiltIn(functionName: String, expr: Expression, args: List<Data>, scope: Scope): Data {
        if (functionName == "car") {
            if (args.size != 1) {
                throw RuntimeException("CAR function expects 1 and only 1 list to be passed.")
            }

            args[0].listValue!!.realize(this, scope)
            if (args[0].listValue!!.realizedData.isEmpty()) {
                throw RuntimeException("CAR cannot be used on the empty list.")
            }
            return args[0].listValue!!.realizedData[0]
        } else if (functionName == "cdr") {
            if (args.size != 1) {
                throw RuntimeException("CDR function expects 1 and only 1 list to be passed.")
            }

            if (args[0].listValue!!.unrealizedItems.isEmpty()) {
                throw RuntimeException("CDR cannot be used on the empty list.")
            }

            val newList = KList(args[0].listValue!!.unrealizedItems.drop(1))
            newList.realize(this, scope)
            return Data(newList)
        } else if (functionName == "cons") {
            if (args.size != 2) {
                throw RuntimeException("CONS function expects 1 list and 1 data object.")
            } else if (args[0].dataType != DataType.LIST) {
                throw RuntimeException("CONS function expects list as first argument.")
            }

            val newListUnrealized = args[0].listValue!!.unrealizedItems + listOf(expr.tail[1])
            val newList = KList(newListUnrealized)
            newList.realize(this, scope) // TODO: This is double-work with line 1 of this function
            return Data(newList)
        }

        throw RuntimeException("Operation $functionName not recognized.")
    }

    private fun handleLogicBuiltIn(functionName: String, args: List<Data>): Data {
        if (!args.all { it.truthyValue != null }) {
            throw RuntimeException("Only numeric types are compatible with *, +, /, and -.")
        }
        val argsAsBools = args.map { it.truthyValue!! }
        return Data(when (functionName) {
            "and" -> argsAsBools.reduce { acc, part -> acc && part }
            "or" -> argsAsBools.reduce { acc, part -> acc || part }
            "not" -> {
                if (args.size != 1) {
                    throw RuntimeException("NOT only accepts one argument.")
                }
                !args[0].truthyValue!!
            }
            else -> throw RuntimeException("$functionName is not a built-in function.")
        })
    }

    private fun handleEqualityBuiltIn(functionName: String, args: List<Data>): Data {
        if (args.size != 2) {
            throw RuntimeException("EQ & NEQ only accept 2 arguments of the same type.")
        }
        return Data(when (functionName) {
            "eq" -> args[0] == args[1]
            "neq" -> args[0] != args[1]
            else -> throw RuntimeException("$functionName is not a built-in function.")
        })
    }

    private fun handleNumericBuiltIn(functionName: String, args: List<Data>): Data {
        if (!args.all { it.numericValue != null }) {
            throw RuntimeException("Only numeric types are compatible with *, +, /, and -.")
        }
        val argsAsNums = args.map { it.numericValue!! }
        return Data(when (functionName) {
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
                if (expr is LetBinding) {
                    expr.execute(this, scope)
                } else {
                    throw RuntimeException("Expected expression '$expr' to be a let-binding.")
                }
            }
            KeywordType.FUN -> {
                val funName = expr.tail[0].name!!
                val f = if (expr.tail.size == 3) {
                    val params = expr.tail[1].list!!
                    val body = expr.tail[2]
                    Function(this, funName, params, body)
                } else {
                    Function(this, funName, KList(emptyList()), expr.tail[1])
                }

                val data = Data(f)
                scope.add(funName, data)
                data
            }
            KeywordType.IF -> {
                if (expr is IfExpression) {
                    expr.execute(this, scope)
                } else {
                    throw RuntimeException("Expected expression '$expr' to be a let-binding.")
                }
            }
        }
    }

    fun realizePart(arg: ExpressionPart, env: Scope): Data {
        return when (arg.type) {
            ExpressionPartType.STRING -> Data(arg.innerText!!)
            ExpressionPartType.BOOLEAN -> Data(arg.truth!!)
            ExpressionPartType.NUMBER -> Data(arg.value!!)
            ExpressionPartType.SYMBOL -> handleSymbol(arg.name!!, env)
            ExpressionPartType.KEYWORD ->
                throw RuntimeException("Encountered free keyword ${arg.keywordType} in the body of an expression")
            ExpressionPartType.EXPRESSION -> execute(arg.expression!!, env)
            ExpressionPartType.LIST -> {
                arg.list!!.realize(this, env)
                return Data(arg.list!!)
            }
        }
    }

    private fun handleSymbol(symbol: String, env: Scope): Data {
        return env.lookup(symbol)
    }
}



