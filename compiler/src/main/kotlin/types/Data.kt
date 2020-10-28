package com.statelesscoder.klisp.compiler.types
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart

enum class DataType {
    STRING,
    NUMBER,
    BOOLEAN,
    LITERAL,
    FUNCTION,
    LIST,
}

class KLString(val text: String) : LiteralValue(DataType.STRING)
class KLNumber(val value: Float) : LiteralValue(DataType.NUMBER)
class KLBool(val truth: Boolean) : LiteralValue(DataType.BOOLEAN)
abstract class LiteralValue(dt: DataType) : Data(dt)

open class Data : ExpressionPart {
    val dataType: DataType
    var literal: LiteralValue? = null
    var stringValue: String? = null
    var numericValue: Float? = null
    var truthyValue: Boolean? = null
    var functionValue: Function? = null
    var listValue: KList? = null

    constructor(literal: LiteralValue) : super()
    {
        this.dataType = DataType.LITERAL
        this.literal = literal
    }

    constructor(value: String) : super()
    {
        this.dataType = DataType.STRING
        this.stringValue = value
    }

    constructor(value: Float) : super()
    {
        this.dataType = DataType.NUMBER
        this.numericValue = value
    }

    constructor(value: Boolean) : super()
    {
        this.dataType = DataType.BOOLEAN
        this.truthyValue = value
    }

    constructor(value: Function) : super()
    {
        this.dataType = DataType.FUNCTION
        this.functionValue = value
    }

    constructor(value: KList) : super()
    {
        this.dataType = DataType.LIST
        this.listValue = value
    }

    constructor(dataType: DataType) : super()
    {
        this.dataType = dataType
    }

    override fun equals(other: Any?): Boolean {
        if (other is Data) {
            return when(dataType) {
                DataType.NUMBER -> numericValue == other.numericValue
                DataType.STRING -> stringValue == other.stringValue
                DataType.BOOLEAN -> truthyValue == other.truthyValue
                DataType.LITERAL -> literal == other.literal
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
        } else {
            return false
        }
    }
    override fun toString(): String = when (dataType) {
        DataType.STRING -> "\"$stringValue\""
        DataType.NUMBER -> "$numericValue"
        DataType.BOOLEAN -> "$truthyValue"
        DataType.FUNCTION -> functionValue.toString()
        DataType.LIST -> listValue.toString()
        DataType.LITERAL -> literal.toString()
    }
}
