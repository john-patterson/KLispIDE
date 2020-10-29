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
        if (expr.head is Keyword) {
            return handleKeyword(expr, env)
        }

        val headResult = realizePart(expr.head, env)
        val realizedArgs = RealizedList(expr.tail.map { realizePart(it, env) })

        if (headResult is Function) {
            return headResult.run(this, realizedArgs, env)
        } else {
            throw RuntimeException("Attempted to invoke a non-function: ${expr.head}.")
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



