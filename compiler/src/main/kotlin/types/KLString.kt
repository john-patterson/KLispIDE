package com.statelesscoder.klisp.compiler.types

class KLString(val text: String) : LiteralValue() {
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