package com.statelesscoder.klisp.compiler.types

class SimpleResult(r: ExecutionResult) {
    val expression = r.expression.toString()
    val result = r.result.toString()
    val scope = r.scope.getBindings()
        .map { Pair(it.first, it.second.toString()) }
        .toMap()
}