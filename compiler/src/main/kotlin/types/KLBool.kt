package com.statelesscoder.klisp.compiler.types

class KLBool(val truth: Boolean) : KLLiteralValue() {
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