package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.types.*

class FunctionDefinition(val name: Symbol, private val params: KList, private val body: ExpressionPart)
    : Expression(ExpressionPart(KeywordType.FUN), listOf(ExpressionPart(name), ExpressionPart(params), body)) {

    override fun execute(executor: Executor, scope: Scope): Data {
        val function = Function(executor, name.symbolName, params, body)
        return Data(function)
    }
}