package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.Symbol
import com.statelesscoder.klisp.compiler.UserDefinedFunction
import com.statelesscoder.klisp.compiler.exceptions.ParsingException
import com.statelesscoder.klisp.compiler.types.KLValue
import com.statelesscoder.klisp.compiler.types.Keyword
import com.statelesscoder.klisp.compiler.types.KeywordType
import com.statelesscoder.klisp.compiler.types.UnrealizedList

class FunctionDefinition(head: ExpressionPart, tail: List<ExpressionPart>) : KeywordExpression(head, tail) {
    private val name: Symbol = tail[0] as Symbol
    private val params: List<Symbol>
    private val body: ExpressionPart

    init {
        if (tail.size == 3) {
            this.params = (tail[1] as UnrealizedList)
                .items
                .map { it as Symbol } // This is asserted as okay in validate
            this.body = tail[2]
        } else {
            this.params = emptyList()
            this.body = tail[1]
        }
    }

    constructor(name: Symbol, params: List<Symbol>, body: ExpressionPart)
        : this(Keyword(KeywordType.FUN_BANG), listOf(name, UnrealizedList(params), body))

    override fun validate(head: ExpressionPart, tail: List<ExpressionPart>) {
        if (head is Keyword
            && head.kwdType == KeywordType.FUN_BANG) {

            if (tail.size == 2 && tail[0] !is Symbol) {
                // This is the case without args
                throw ParsingException("Encountered function without symbol as name: $tail")
            } else if (tail.size == 3) { // This has all parts
                // This is the case without args
                val nameValid = tail[0] is Symbol
                val argsValid = tail[1] is UnrealizedList
                        && (tail[1] as UnrealizedList).items.all { it is Symbol }

                if (!nameValid) {
                    throw ParsingException("Encountered function without symbol as name: $tail")
                } else if (!argsValid) {
                    throw ParsingException("Encountered function with invalid item in arg list: $tail")
                }
            } else if (tail.size < 2) {
                throw ParsingException("Encountered function with too few parts: $tail")
            } else if (tail.size > 3) {
                throw ParsingException("Encountered function with too many parts: $tail")
            }
        }
    }

    override fun execute(executor: Executor, scope: Scope): KLValue {
        val function = UserDefinedFunction(name, params, body, scope)
        scope.add(name, function)
        return function
    }
}