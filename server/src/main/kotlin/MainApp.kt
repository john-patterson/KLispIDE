package com.statelesscoder.klisp.server

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.statelesscoder.klisp.compiler.*
import io.javalin.Javalin

fun main(args: Array<String>) {
    val app = Javalin.create().start(7340)
    val router = Routes()
    app.post("/tokenize") {
        ctx -> ctx.json(router.tokenize(ctx.body()))
    }

    app.post("/parse") {
            ctx -> ctx.json(router.parse(ctx.body()).toString())
    }

    app.post("/execute") { ctx ->
        ctx.json(router.execute(ctx.body()))
    }
}

class Routes {
    private val tokenizer = Tokenizer()
    private val parser = Parser()
    private val executor = Executor()

    fun tokenize(request: String): List<Token> {
        return tokenizer.scan(request)
    }

    fun parse(request: String): Expression {
        val tokens = tokenize(request)
        return parser.parseSingleExpression(tokens)
    }

    fun execute(request: String): SimpleResult {
        return SimpleResult(runCode(request))
    }
}

