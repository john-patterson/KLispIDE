package com.statelesscoder.klisp.compiler

class ScopeDataException(message: String): Exception(message)

class Scope {
    private var symbolTable: MutableMap<String, Data> = mutableMapOf()

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