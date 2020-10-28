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
        } else if (other is Data && other.dataType == DataType.LITERAL) {
            this.text == (other.literal as KLString).text
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
        } else if (other is Data && other.dataType == DataType.LITERAL && other.literal is KLNumber) {
            this.value == (other.literal as KLNumber).value
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
        } else if (other is Data && other.dataType == DataType.LITERAL && other.literal is KLBool) {
            this.truth == (other.literal as KLBool).truth
        } else {
            false
        }
    }
    override fun toString(): String {
        return truth.toString()
    }
}
abstract class LiteralValue(dt: DataType) : Data(dt) {
    init {
        this.literal = this
    }
}

open class Data : ExpressionPart {
    val dataType: DataType
    var literal: LiteralValue? = null
    var functionValue: Function? = null
    var listValue: RealizedList? = null

    constructor(literal: LiteralValue) : super()
    {
        this.dataType = DataType.LITERAL
        this.literal = literal
    }

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
                DataType.LITERAL -> literal == other.literal
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
        DataType.LITERAL -> literal.toString()
    }
}
