package com.statelesscoder.klisp.server

import com.statelesscoder.klisp.compiler.*
import io.javalin.Javalin
import io.javalin.http.Context

fun main(args: Array<String>) {
    val app = Javalin.create().start(7340)
    val router = Routes()
    app.post("/tokenize") {
        ctx -> ctx.json(router.tokenize(ctx.body()))
    }

    app.post("/parse") {
            ctx -> ctx.json(router.parse(ctx.body()))
    }

    app.post("/execute") { ctx ->
        val scope = ctx.formParam("scope", Scope::class.java)
        if (scope.isValid()) {
            ctx.json(router.execute(ctx.body(), scope.value!!))
        } else {
            ctx.json(router.execute(ctx.body()))
        }
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
        return parser.parse(tokens)
    }

    fun execute(request: String, scope: Scope = Scope()): Data {
        val tokens = tokenize(request)
        val ast = parser.parse(tokens)
        return executor.execute(ast, scope)
    }
}