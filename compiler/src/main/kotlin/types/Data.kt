package com.statelesscoder.klisp.compiler.types
import com.statelesscoder.klisp.compiler.Function

enum class DataType {
    STRING,
    NUMBER,
    BOOLEAN,
    FUNCTION,
    LIST,
}


fun dataTypeToExpressionPartType(type: DataType): ExpressionPartType {
    return when (type) {
        DataType.BOOLEAN -> ExpressionPartType.BOOLEAN
        DataType.FUNCTION -> ExpressionPartType.EXPRESSION
        DataType.LIST -> ExpressionPartType.LIST
        DataType.NUMBER -> ExpressionPartType.NUMBER
        DataType.STRING -> ExpressionPartType.STRING
    }
}

class Data : ExpressionPart {
    val dataType: DataType
    var stringValue: String? = null
    var numericValue: Float? = null
    var truthyValue: Boolean? = null
    var functionValue: Function? = null
    var listValue: KList? = null

    constructor(value: String) : super(value)
    {
        this.dataType = DataType.STRING
        this.stringValue = value
    }

    constructor(value: Float) : super(value)
    {
        this.dataType = DataType.NUMBER
        this.numericValue = value
    }

    constructor(value: Boolean) : super(value)
    {
        this.dataType = DataType.BOOLEAN
        this.truthyValue = value
    }

    constructor(value: Function) : super(value)
    {
        this.dataType = DataType.FUNCTION
        this.functionValue = value
    }

    constructor(value: KList) : super(value)
    {
        this.dataType = DataType.LIST
        this.listValue = value
    }

    constructor(dataType: DataType) : super(dataTypeToExpressionPartType(dataType))
    {
        this.dataType = dataType
    }

    override fun equals(other: Any?): Boolean {
        if (other as Data == null || other.dataType != dataType) {
            return false
        } else {
            return when(dataType) {
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
    override fun toString(): String = when (dataType) {
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
