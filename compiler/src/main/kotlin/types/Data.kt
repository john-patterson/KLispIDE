package com.statelesscoder.klisp.compiler.types
import com.statelesscoder.klisp.compiler.Function

enum class DataType {
    STRING,
    NUMBER,
    BOOLEAN,
    FUNCTION,
    LIST,
}

class Data(val type: DataType) {
    var stringValue: String? = null
    var numericValue: Float? = null
    var truthyValue: Boolean? = null
    var functionValue: Function? = null
    var listValue: KList? = null

    override fun equals(other: Any?): Boolean {
        if (other as Data == null || other.type != type) {
            return false
        } else {
            return when(type) {
                DataType.NUMBER -> numericValue == other.numericValue
                DataType.STRING -> stringValue == other.stringValue
                DataType.BOOLEAN -> truthyValue == other.truthyValue
                DataType.FUNCTION -> functionValue!!.name == other.functionValue!!.name
                DataType.LIST -> {
                    if (this.listValue!!.realizedData.size != other.listValue!!.realizedData.size) {
                        return false
                    } else {
                        for (i in this.listValue!!.realizedData.indices) {
                            if (this.listValue!!.realizedData[i] != other.listValue!!.realizedData[i]) {
                                return false
                            }
                        }
                        return true
                    }
                }
            }
        }
    }
    override fun toString(): String = when (type) {
        DataType.STRING -> "\"$stringValue\""
        DataType.NUMBER -> "$numericValue"
        DataType.BOOLEAN -> "$truthyValue"
        DataType.FUNCTION -> functionValue.toString()
        DataType.LIST -> listValue.toString()
    }
}

fun createData(s: String): Data {
    val d =
        Data(DataType.STRING)
    d.stringValue = s
    return d
}

fun createData(n: Float): Data {
    val d =
        Data(DataType.NUMBER)
    d.numericValue = n
    return d
}

fun createData(b: Boolean): Data {
    val d =
        Data(DataType.BOOLEAN)
    d.truthyValue = b
    return d
}

fun createData(f: Function): Data {
    val d =
        Data(DataType.FUNCTION)
    d.functionValue = f
    return d
}

fun createData(l: KList): Data {
    val d =
        Data(DataType.LIST)
    d.listValue = l
    return d
}
