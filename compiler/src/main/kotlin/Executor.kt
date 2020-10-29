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

        if (headResult is UserDefinedFunction) {
            return headResult.run(this, argsData, env)
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
        val args = RealizedList(expr.tail.map { execute(it, scope) })
        if (expr.head is Symbol) {
            val functionName = expr.head.symbolName.toLowerCase()

            if (functionName == "print") {
                val printFn = PrintFunction()
                return printFn.run(this, args, scope)
            }

            if (listBuiltins.contains(functionName)) {
                return handleListBuiltIn(functionName, args, scope)
            }

            if (logicBuiltins.contains(functionName)) {
                return handleLogicBuiltIn(functionName, args, scope)
            }

            if (equalityBuiltins.contains(functionName)) {
                return handleEqualityBuiltIn(functionName, args, scope)
            }

            return handleNumericBuiltIn(functionName, args, scope)
        } else {
            throw RuntimeException("Expression $expr should start with a symbol.")
        }
    }

    private fun handleListBuiltIn(functionName: String, args: RealizedList, scope: Scope): KLValue {
        if (functionName == "car") {
            val carFn = CarFunction()
            return carFn.run(this, args, scope)
        } else if (functionName == "cdr") {
            val cdrFn = CdrFunction()
            return cdrFn.run(this, args, scope)
        } else if (functionName == "cons") {
            val consFn = ConsFunction()
            return consFn.run(this, args, scope)
        }

        throw RuntimeException("Operation $functionName not recognized.")
    }

    private fun handleLogicBuiltIn(functionName: String, args: RealizedList, scope: Scope): KLBool {
        return when (functionName) {
            "and" -> AndFunction().run(this, args, scope)
            "or" -> OrFunction().run(this, args, scope)
            "not" -> NotFunction().run(this, args, scope)
            else -> throw RuntimeException("$functionName is not a built-in function.")
        }
    }

    private fun handleEqualityBuiltIn(functionName: String, args: RealizedList, scope: Scope): KLBool {
        return when (functionName) {
            "eq" -> EqFunction().run(this, args, scope)
            "neq" -> NeqFunction().run(this, args, scope)
            else -> throw RuntimeException("$functionName is not a built-in function.")
        }
    }

    private fun handleNumericBuiltIn(functionName: String, args: RealizedList, scope: Scope): KLValue {
        return when (functionName) {
            "*" -> MulFunctions().run(this, args, scope)
            "+" -> AddFunctions().run(this, args, scope)
            "-" -> SubFunctions().run(this, args, scope)
            "/" -> DivFunctions().run(this, args, scope)
            else -> throw RuntimeException("$functionName is not a built-in function.")
        }
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
            is UserDefinedFunction -> execute(arg, env)
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



