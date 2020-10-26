package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.ScopeDataException
import com.statelesscoder.klisp.compiler.types.Data

class Scope {
    private var symbolTable: MutableMap<String, Data> = mutableMapOf()

    fun getBindings(): List<Pair<String, Data>> = symbolTable.entries
        .map { Pair(it.key, it.value) }

    fun lookup(symbolName: String): Data {
        return symbolTable.getOrElse(symbolName) {
            throw ScopeDataException("Failed to find symbol $symbolName.")
        }
    }

    fun add(symbolName: String, value: Data) {
        symbolTable[symbolName] = value
    }

    constructor() {}
    constructor(otherScope: Scope) {
        this.symbolTable.putAll(otherScope.symbolTable)
    }
    constructor(data: Map<String, Data>) {
        this.symbolTable.putAll(data)
    }
}