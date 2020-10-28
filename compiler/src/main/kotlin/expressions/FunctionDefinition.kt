package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.Symbol
import com.statelesscoder.klisp.compiler.types.*

class FunctionDefinition(val name: Symbol, private val params: KList, private val body: ExpressionPart)
    : Expression(Keyword(KeywordType.FUN), listOf(name, params, body)) {

    override fun execute(executor: Executor, scope: Scope): Data {
        val function = Function(executor, name, params, body)
        val data = Data(function)
        scope.add(name, data)
        return data
    }
}