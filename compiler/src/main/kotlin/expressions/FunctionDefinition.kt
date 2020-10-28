package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.Symbol
import com.statelesscoder.klisp.compiler.types.*

class FunctionDefinition(val name: Symbol, private val params: List<Symbol>, private val body: ExpressionPart)
    : Expression(Keyword(KeywordType.FUN), listOf(name, UnrealizedList(params), body)) {

    override fun execute(executor: Executor, scope: Scope): KLValue {
        val function = Function(executor, name, params, body)
        scope.add(name, function)
        return function
    }
}