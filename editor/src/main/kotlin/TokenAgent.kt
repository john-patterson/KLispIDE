package com.statelesscoder.klisp.editor

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.statelesscoder.klisp.compiler.ExecutionResult
import com.statelesscoder.klisp.compiler.SimpleResult
import com.statelesscoder.klisp.compiler.Token

interface TokenSource {
    fun getTokens(text: String): Array<Token>
    fun execute(text: String): Array<SimpleResult>
}

class TokenAgentException(message: String) : Exception(message) {
}

class TokenAgent: TokenSource {
    private val baseUrl = "http://localhost:7340"

    override fun getTokens(text: String): Array<Token> {
        var tokenArray: Array<Token> = emptyArray()
        var shouldThrow: Boolean = false
        var errorString: String = ""

        val httpAsync = "$baseUrl/tokenize"
            .httpPost()
            .body(text)
            .responseString { _, response, result ->
                when (result) {
                    is Result.Failure -> {
                        shouldThrow = true
                        errorString = "Could not tokenize, got result code ${response.statusCode}: ${result.error.message}"
                    }
                    is Result.Success -> {
                        val data = result.get()
                        tokenArray = Gson()
                            .fromJson(data, Array<Token>::class.java)
                    }
                }
            }

        httpAsync.join()

        if (shouldThrow) {
            throw TokenAgentException(errorString)
        }

        return tokenArray
    }

    override fun execute(text: String): Array<SimpleResult> {
        var executionResult: Array<SimpleResult>? = null
        var shouldThrow = false
        var errorString = ""

        val httpAsync = "$baseUrl/execute"
            .httpPost()
            .body(text)
            .responseString { _, response, result ->
                when (result) {
                    is Result.Failure -> {
                        shouldThrow = true
                        errorString = "Could not execute, got result code ${response.statusCode}: ${result.error.message}"
                    }
                    is Result.Success -> {
                        val data = result.get()
                        executionResult = Gson()
                            .fromJson(data, Array<SimpleResult>::class.java)
                    }
                }
            }

        httpAsync.join()

        if (shouldThrow) {
            throw TokenAgentException(errorString)
        }

        return executionResult!!
    }
}