package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.ScopeDataException
import com.statelesscoder.klisp.compiler.types.KLValue

class Scope {
    private var symbolTable: MutableMap<String, KLValue> = mutableMapOf()

    fun getBindings(): List<Pair<String, KLValue>> = symbolTable.entries
        .map { Pair(it.key, it.value) }

    fun lookup(symbol: Symbol): KLValue {
        return symbolTable.getOrElse(symbol.symbolName) {
            throw ScopeDataException("Failed to find symbol ${symbol.symbolName}.")
        }
    }

    fun add(symbol: Symbol, value: KLValue) {
        symbolTable[symbol.symbolName] = value
    }

    constructor() {}
    constructor(otherScope: Scope) {
        this.symbolTable.putAll(otherScope.symbolTable)
    }
    constructor(klValue: Map<String, KLValue>) {
        this.symbolTable.putAll(klValue)
    }
}