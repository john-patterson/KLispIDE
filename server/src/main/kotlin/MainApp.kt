package com.statelesscoder.klisp.server

import io.javalin.Javalin

fun main(args: Array<String>) {
    val app = Javalin.create().start(7340)
    app.get("/") { ctx -> ctx.result("Hello World!") }

}