package com.statelesscoder.klisp.compiler.types

class KLNumber(val value: Float) : LiteralValue(
    DataType.LITERAL
) {
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