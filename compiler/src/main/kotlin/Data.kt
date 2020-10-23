package com.statelesscoder.klisp.compiler

class RuntimeException(message: String): Exception(message)


enum class DataType {
    STRING,
    NUMBER,
    BOOLEAN,
    FUNCTION,
}

class Data(val type: DataType) {
    var stringValue: String? = null
    var numericValue: Float? = null
    var truthyValue: Boolean? = null
    var functionValue: Function? = null
}

fun stringData(s: String): Data {
    val d = Data(DataType.STRING)
    d.stringValue = s
    return d
}

fun numericData(n: Float): Data {
    val d = Data(DataType.NUMBER)
    d.numericValue = n
    return d
}

fun truthyData(b: Boolean): Data {
    val d = Data(DataType.BOOLEAN)
    d.truthyValue = b
    return d
}

fun functionData(f: Function): Data {
    val d = Data(DataType.FUNCTION)
    d.functionValue = f
    return d
}
