package com.statelesscoder.klisp.compiler.types
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart

enum class DataType {
    LITERAL,
    FUNCTION,
    LIST,
}

open class KLValue : ExpressionPart {
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

    constructor(items: List<KLValue>) {
        this.dataType = DataType.LIST
        this.listValue = RealizedList(items)
    }

    override fun equals(other: Any?): Boolean {
        if (other is KLValue) {
            return when(dataType) {
                DataType.LITERAL -> (this as KLLiteralValue) == other
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
        DataType.LITERAL -> (this as KLLiteralValue).toString()
    }
}
