package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.ScopeDataException
import com.statelesscoder.klisp.compiler.types.Data

class Scope {
    private var symbolTable: MutableMap<String, Data> = mutableMapOf()

    fun getBindings(): List<Pair<String, Data>> = symbolTable.entries
        .map { Pair(it.key, it.value) }

    fun lookup(symbol: Symbol): Data {
        return symbolTable.getOrElse(symbol.symbolName) {
            throw ScopeDataException("Failed to find symbol ${symbol.symbolName}.")
        }
    }

    fun add(symbol: Symbol, value: Data) {
        symbolTable[symbol.symbolName] = value
    }

    constructor() {}
    constructor(otherScope: Scope) {
        this.symbolTable.putAll(otherScope.symbolTable)
    }
    constructor(data: Map<String, Data>) {
        this.symbolTable.putAll(data)
    }
}