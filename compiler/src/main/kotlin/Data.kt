package com.statelesscoder.klisp.compiler

class RuntimeException(message: String): Exception(message)

class Function(private val executor: Executor,
               private val name: String,
               private val params: List<ExpressionPart>,
               private val body: ExpressionPart) {
    fun run(args: List<Data>, scope: Scope = Scope()): Data {
        if (args.size != params.size) {
            throw RuntimeException("Function '$name' expects '${params.size}' arguments, but got '${args.size}'.")
        }

        val boundScope = Scope(scope)
        for (i in args.indices) {
            boundScope.add(params[i].name!!, args[i])
        }

        return when (body.type) {
            ExpressionPartType.BOOLEAN, ExpressionPartType.NUMBER, ExpressionPartType.SYMBOL, ExpressionPartType.STRING ->
                executor.realizePart(body, boundScope).data
            ExpressionPartType.EXPRESSION, ExpressionPartType.KEYWORD ->
                executor.execute(body.expression!!, boundScope).data
        }
    }
}


enum class DataType {
    STRING,
    NUMBER,
    BOOLEAN,
    FUNCTION,
}

class Data(val type: DataType) {
    var stringValue: String? = null
    var numericValue: Float? = null
    var truthyValue: Boolean? = null
    var functionValue: Function? = null
}

fun stringData(s: String): Data {
    val d = Data(DataType.STRING)
    d.stringValue = s
    return d
}

fun numericData(n: Float): Data {
    val d = Data(DataType.NUMBER)
    d.numericValue = n
    return d
}

fun truthyData(b: Boolean): Data {
    val d = Data(DataType.BOOLEAN)
    d.truthyValue = b
    return d
}

fun functionData(f: Function): Data {
    val d = Data(DataType.FUNCTION)
    d.functionValue = f
    return d
}
