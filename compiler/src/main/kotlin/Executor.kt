package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.expressions.*
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
    fun execute(part: ExpressionPart, env: Scope = Scope()): KLValue = realizePart(part, env)
    fun execute(expr: Expression, env: Scope = Scope()): KLValue {
        if (expr.head is Symbol && builtinFunctions.contains(expr.head.symbolName.toLowerCase())) {
            return handleBuiltinFunction(expr, env)
        } else if (expr.head is Keyword) {
            return handleKeyword(expr, env)
        }

        val headResult = realizePart(expr.head, env)
        val argsResults = expr.tail.map { Pair(it, realizePart(it, env)) }
        val argsData = RealizedList(argsResults.map { it.second })

        if (headResult is Function) {
            return headResult.run(argsData, env)
        } else {
            throw RuntimeException("Attempted to invoke a non-function: ${expr.head}.")
        }
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
    private fun handleBuiltinFunction(expr: Expression, scope: Scope): KLValue {
        val args = expr.tail.map { execute(it, scope) }
        if (expr.head is Symbol) {
            val functionName = expr.head.symbolName.toLowerCase()

            if (functionName == "print") {
                if (!args.all { it is KLString }) {
                    throw RuntimeException("Only strings are printable.")
                }

                val stringArgs = args.map { it as KLString }.map { it.text }
                val s = stringArgs.reduce() {acc, s -> "$acc $s" }
                print(s)
                return KLString(s)
            }

            if (listBuiltins.contains(functionName)) {
                return handleListBuiltIn(functionName, args)
            }

            if (logicBuiltins.contains(functionName)) {
                return handleLogicBuiltIn(functionName, args)
            }

            if (equalityBuiltins.contains(functionName)) {
                return handleEqualityBuiltIn(functionName, args)
            }

            return handleNumericBuiltIn(functionName, args)
        } else {
            throw RuntimeException("Expression $expr should start with a symbol.")
        }
    }

    private fun handleListBuiltIn(functionName: String, args: List<KLValue>): KLValue {
        if (functionName == "car") {
            if (args.size != 1) {
                throw RuntimeException("CAR function expects 1 and only 1 list to be passed.")
            }

            val functionArgs = args[0]
            if (functionArgs is RealizedList && functionArgs.items.isNotEmpty()) {
                return functionArgs.items[0]
            } else {
                throw RuntimeException("CAR does not support these args: $args")
            }
        } else if (functionName == "cdr") {
            if (args.size != 1) {
                throw RuntimeException("CDR function expects 1 and only 1 list to be passed.")
            }

            val functionArgs = args[0]
            if (functionArgs is RealizedList && functionArgs.items.isNotEmpty()) {
                return RealizedList(functionArgs.items.drop(1))
            } else {
                throw RuntimeException("CAR does not support these args: $args")
            }
        } else if (functionName == "cons") {
            if (args.size != 2) {
                throw RuntimeException("CONS function expects 1 list and 1 data object.")
            }
            val list = args[0]
            val value = args[1]
            if (list is RealizedList) {
                return RealizedList(list.items + listOf(value))
            } else {
                throw RuntimeException("CONS function expects list as first argument.")
            }
        }

        throw RuntimeException("Operation $functionName not recognized.")
    }

    private fun handleLogicBuiltIn(functionName: String, args: List<KLValue>): KLBool {
        if (!args.all { it is KLBool }) {
            throw RuntimeException("Only numeric types are compatible with *, +, /, and -.")
        }
        val argsAsBools = args.map { (it as KLBool).truth }
        return KLBool(when (functionName) {
            "and" -> argsAsBools.reduce { acc, part -> acc && part }
            "or" -> argsAsBools.reduce { acc, part -> acc || part }
            "not" -> {
                if (args.size != 1) {
                    throw RuntimeException("NOT only accepts one argument.")
                }
                !argsAsBools[0]
            }
            else -> throw RuntimeException("$functionName is not a built-in function.")
        })
    }

    private fun handleEqualityBuiltIn(functionName: String, args: List<KLValue>): KLBool {
        if (args.size != 2) {
            throw RuntimeException("EQ & NEQ only accept 2 arguments of the same type.")
        }
        return KLBool(when (functionName) {
            "eq" -> args[0] == args[1]
            "neq" -> args[0] != args[1]
            else -> throw RuntimeException("$functionName is not a built-in function.")
        })
    }

    private fun handleNumericBuiltIn(functionName: String, args: List<KLValue>): KLValue {
        if (!args.all { it is KLNumber }) {
            throw RuntimeException("Only numeric types are compatible with *, +, /, and -.")
        }
        val argsAsNums = args.map { (it as KLNumber).value }
        return KLNumber(when (functionName) {
            "*" -> argsAsNums.reduce { acc, number -> acc * number }
            "+" -> argsAsNums.reduce { acc, number -> acc + number }
            "-" -> argsAsNums.reduce { acc, number -> acc - number }
            "/" -> argsAsNums.reduce { acc, number -> acc / number }
            else -> throw RuntimeException("$functionName is not a built-in function.")
        })
    }

    private fun handleKeyword(expr: Expression, scope: Scope): KLValue {
        return when (expr) {
            is FunctionDefinition -> expr.execute(this, scope)
            is IfExpression -> expr.execute(this, scope)
            is LetBinding -> expr.execute(this, scope)
            else -> throw RuntimeException("Expected expression '$expr' to be a special contruct.")
        }
    }

    fun realizePart(arg: ExpressionPart, env: Scope): KLValue {
        return when (arg) {
            is KLLiteralValue -> arg
            is RealizedList -> arg
            is Function -> execute(arg, env)
            is Symbol -> handleSymbol(arg, env)
            is Keyword -> throw RuntimeException("Encountered free keyword ${arg.kwdType} in the body of an expression")
            is Expression -> execute(arg, env)
            is UnrealizedList -> arg.realize(this, env)
            else -> throw RuntimeException("Part $arg not recognized.")
        }
    }

    private fun handleSymbol(symbol: Symbol, env: Scope): KLValue {
        return env.lookup(symbol)
    }
}



