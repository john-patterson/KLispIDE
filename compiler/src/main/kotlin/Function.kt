package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.types.KLValue
import com.statelesscoder.klisp.compiler.types.RealizedList

abstract class Function : KLValue() {
    abstract fun run(executor: Executor, args: RealizedList, scope: Scope = Scope()): KLValue
}