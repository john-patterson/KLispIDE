package com.statelesscoder.klisp.compiler

import javax.xml.crypto.Data

class Executor {
    fun execute(expr: Expression): ExecutionResult {
//        return when (expr.head.type) {
//            ExpressionPartType.EXPRESSION -> ExecutionResult()
//            ExpressionPartType.SYMBOL -> ExecutionResult()
//            ExpressionPartType.KEYWORD -> when (expr.head.keywordType) {
//                KeywordType.FUN -> ExecutionResult()
//                KeywordType.IF -> ExecutionResult()
//                KeywordType.LET -> ExecutionResult()
//                else -> throw RuntimeException("Unknown keyword type ${expr.head.keywordType}.")
//            }
//            else -> throw RuntimeException("Invalid beginning to function call ${expr.head}.")
//        }
        return realizeArgs(expr.tail)[0]
    }

    fun realizeArgs(args: List<ExpressionPart>): List<ExecutionResult> {
        return args
            .map { realizePart(it) }
    }

    fun realizePart(arg: ExpressionPart): ExecutionResult {
        return when (arg.type) {
            ExpressionPartType.STRING -> stringValue(arg.innerText!!)
            ExpressionPartType.BOOLEAN -> booleanValue(arg.truth!!)
            ExpressionPartType.NUMBER -> numberValue(arg.value!!)
            ExpressionPartType.EXPRESSION -> execute(arg.expression!!)
            ExpressionPartType.KEYWORD -> handleKeyword(arg)
            ExpressionPartType.SYMBOL -> handleSymbol(arg)
        }
    }

    fun handleKeyword(kwd: ExpressionPart): ExecutionResult {
        return ExecutionResult(false, DataType.BOOLEAN)
    }

    fun handleSymbol(kwd: ExpressionPart): ExecutionResult {
        return ExecutionResult(false, DataType.BOOLEAN)
    }
}

fun stringValue(text: String): ExecutionResult {
    val result = ExecutionResult(true, DataType.STRING)
    result.innerText = text
    return result
}

fun numberValue(number: Number): ExecutionResult {
    val result = ExecutionResult(true, DataType.NUMBER)
    result.innerValue = number
    return result
}

fun booleanValue(truth: Boolean): ExecutionResult {
    val result = ExecutionResult(true, DataType.BOOLEAN)
    result.innerTruth = truth
    return result
}

class RuntimeException(message: String): Exception(message)

enum class DataType {
    STRING,
    NUMBER,
    BOOLEAN,
}

class ExecutionResult(val successful: Boolean, val type: DataType) {
    var innerText: String? = null
    var innerValue: Number? = null
    var innerTruth: Boolean? = null
}




