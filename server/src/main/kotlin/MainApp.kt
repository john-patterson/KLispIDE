package com.statelesscoder.klisp.server

import com.statelesscoder.klisp.compiler.Expression
import com.statelesscoder.klisp.compiler.Parser
import com.statelesscoder.klisp.compiler.Token
import com.statelesscoder.klisp.compiler.Tokenizer
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
}

class Routes {
    private val tokenizer = Tokenizer()
    private val parser = Parser()

    fun tokenize(request: String): List<Token> {
        return tokenizer.scan(request)
    }

    fun parse(request: String): Expression {
        val tokens = tokenize(request)
        return parser.parse(tokens)
    }

}