package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.ScopeDataException
import com.statelesscoder.klisp.compiler.types.KLValue

class Scope {
    private var symbolTable: MutableMap<String, KLValue> = mutableMapOf()

    init {
        BuiltInFunction.getAllBuiltInFunctions()
            .forEach { add(it.name, it) }
    }

    fun getBindings(): List<Pair<String, KLValue>> = symbolTable.entries
        .map { Pair(it.key, it.value) }

    fun lookup(symbol: Symbol): KLValue {
        return symbolTable.getOrElse(symbol.symbolName.toLowerCase()) {
            throw ScopeDataException("Failed to find symbol ${symbol.symbolName}.")
        }
    }

    fun add(symbol: Symbol, value: KLValue) {
        symbolTable[symbol.symbolName.toLowerCase()] = value
    }

    constructor() {}
    constructor(vararg otherScopes: Scope) {
        otherScopes
            .map { it.symbolTable }
            .forEach { this.symbolTable.putAll(it)}
    }
    constructor(klValue: Map<String, KLValue>) {
        this.symbolTable.putAll(klValue)
    }
}