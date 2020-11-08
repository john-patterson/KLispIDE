package com.statelesscoder.klisp.compiler.expressions

import com.statelesscoder.klisp.compiler.AnonymousFunction
import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.Symbol
import com.statelesscoder.klisp.compiler.exceptions.ParsingException
import com.statelesscoder.klisp.compiler.types.KLValue
import com.statelesscoder.klisp.compiler.types.Keyword
import com.statelesscoder.klisp.compiler.types.KeywordType
import com.statelesscoder.klisp.compiler.types.UnrealizedList

class AnonymousFunctionDefinition(head: ExpressionPart, tail: List<ExpressionPart>) : KeywordExpression(head, tail) {
    private val params: List<Symbol>
    private val body: ExpressionPart

    init {
        if (tail.size == 2) {
            this.params = (tail[0] as UnrealizedList)
                .items
                .map { it as Symbol }
            this.body = tail[1]
        } else {
            this.params = emptyList()
            this.body = tail[0]
        }
    }

    override fun validate(head: ExpressionPart, tail: List<ExpressionPart>) {
        if (head is Keyword && head.kwdType == KeywordType.FUN) {
            if (tail.isEmpty() || tail.size > 2) {
                throw ParsingException("Function ($head $tail) has an inappropriately sized tail.")
            } else if (tail.size == 2) {
                val paramList = tail[0]
                if (paramList !is UnrealizedList || paramList.items.any { it !is Symbol }) {
                    throw ParsingException("Function ($head $tail) has a bad parameter list.")
                }
            }
        }
    }

    override fun execute(executor: Executor, scope: Scope): KLValue {
        return AnonymousFunction(this.params, this.body, scope)
    }
}