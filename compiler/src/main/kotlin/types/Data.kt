package com.statelesscoder.klisp.compiler.types
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart

enum class DataType {
    LITERAL,
    FUNCTION,
    LIST,
}

class KLString(val text: String) : LiteralValue(DataType.LITERAL) {
    override fun equals(other: Any?): Boolean {
        return if (other is KLString) {
            this.text == other.text
        } else {
            false
        }
    }
    override fun toString(): String {
        return "\"$text\""
    }
}
class KLNumber(val value: Float) : LiteralValue(DataType.LITERAL) {
    override fun equals(other: Any?): Boolean {
        return if (other is KLNumber) {
            this.value == other.value
        } else {
            false
        }
    }
    override fun toString(): String {
        return value.toString()
    }
}
class KLBool(val truth: Boolean) : LiteralValue(DataType.LITERAL) {
    override fun equals(other: Any?): Boolean {
        return if (other is KLBool) {
            this.truth == other.truth
        } else {
            false
        }
    }
    override fun toString(): String {
        return truth.toString()
    }
}
abstract class LiteralValue(dt: DataType) : Data(dt)

open class Data : ExpressionPart {
    val dataType: DataType
    var functionValue: Function? = null
    var listValue: RealizedList? = null

    constructor(value: Function) : super()
    {
        this.dataType = DataType.FUNCTION
        this.functionValue = value
    }

    constructor(value: RealizedList) : super()
    {
        this.dataType = DataType.LIST
        this.listValue = value
    }

    constructor(dataType: DataType) : super()
    {
        this.dataType = dataType
    }

    constructor(items: List<Data>) {
        this.dataType = DataType.LIST
        this.listValue = RealizedList(items)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Data) {
            return when(dataType) {
                DataType.LITERAL -> (this as LiteralValue) == other
                DataType.FUNCTION -> functionValue!!.name == other.functionValue!!.name
                DataType.LIST -> {
                    if (this.listValue!!.items.size != other.listValue!!.items.size) {
                        return false
                    } else {
                        for (i in this.listValue!!.items.indices) {
                            if (this.listValue!!.items[i] != other.listValue!!.items[i]) {
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
        DataType.FUNCTION -> functionValue.toString()
        DataType.LIST -> listValue.toString()
        DataType.LITERAL -> (this as LiteralValue).toString()
    }
}
