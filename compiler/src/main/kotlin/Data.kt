package com.statelesscoder.klisp.compiler
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

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

    override fun toString(): String = when (type) {
        DataType.STRING -> "\"$stringValue\""
        DataType.NUMBER -> "$numericValue"
        DataType.BOOLEAN -> "$truthyValue"
        DataType.FUNCTION -> "$functionValue"
    }
}

fun createData(s: String): Data {
    val d = Data(DataType.STRING)
    d.stringValue = s
    return d
}

fun createData(n: Float): Data {
    val d = Data(DataType.NUMBER)
    d.numericValue = n
    return d
}

fun createData(b: Boolean): Data {
    val d = Data(DataType.BOOLEAN)
    d.truthyValue = b
    return d
}

fun createData(f: Function): Data {
    val d = Data(DataType.FUNCTION)
    d.functionValue = f
    return d
}
