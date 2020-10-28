package com.statelesscoder.klisp.compiler.types

import com.statelesscoder.klisp.compiler.Scope
import com.statelesscoder.klisp.compiler.expressions.Expression

data class ExecutionResult(val expression: Expression, val result: Data, val scope: Scope)