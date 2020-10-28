package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.Symbol
import com.statelesscoder.klisp.compiler.types.*

class FunctionDefinition(val name: Symbol, private val params: KList, private val body: ExpressionPart)
    : Expression(ExpressionPart(KeywordType.FUN), listOf(ExpressionPart(name), ExpressionPart(params), body)) {

    override fun execute(executor: Executor, scope: Scope): Data {
        val function = Function(executor, name.symbolName, params, body)
        val data = Data(function)
        scope.add(name.symbolName, data)
        return data
    }
}