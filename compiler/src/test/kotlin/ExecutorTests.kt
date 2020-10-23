import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.Function
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExecutorTests {
    @Nested
    inner class BuiltinTests {
        @Test
        fun `builtin functions work with simple types`() {
            assertEquals(400f, testBuiltin("*", 10f, 20f, 2f).numericValue)
            assertEquals(32f, testBuiltin("+", 10f, 20f, 2f).numericValue)
            assertEquals(-12f, testBuiltin("-", 10f, 20f, 2f).numericValue)
            assertEquals(0.25f, testBuiltin("/", 10f, 20f, 2f).numericValue)
        }

        @Test
        fun `builtin functions work with complex types`() {
            val e = Executor()
            val expr = Expression(
                symbolPart("*"), listOf(
                    expressionPart(
                        Expression(
                            symbolPart("+"), listOf(
                                numericPart(1f),
                                numericPart(2f)
                            )
                        )
                    ),
                    numericPart(10f)
                )
            )
            assertEquals(30f, e.execute(expr).numericValue)
        }

        @Test
        fun `builtin print function concatenates strings`() {
            val e = Executor()
            val result = e.execute(
                Expression(
                    symbolPart("pRINt"),
                    listOf(stringPart("hello"), stringPart("world"))
                )
            )
            assertEquals(DataType.STRING, result.type)
            assertEquals("hello world", result.stringValue)
        }

        @Test
        fun `builtin print function fails on non-strings`() {
            val e = Executor()

            assertThrows(RuntimeException::class.java) {
                e.execute(
                    Expression(
                        symbolPart("pRINt"),
                        listOf(stringPart("hello"), stringPart("world"), numericPart(2f))
                    )
                )
            }
        }

        @Test
        fun `builtin print function succeeds on complex objects which evaluate to strings`() {
            val e = Executor()
            val scope = Scope()
            val id = Function(e, "id", listOf(symbolPart("x")), symbolPart("x"))
            scope.add("id", createData(id))

            val expr = Expression(symbolPart("id"), listOf(stringPart("test")))
            val result = e.execute(
                Expression(
                    symbolPart("pRINt"),
                    listOf(stringPart("hello"), stringPart("world"), expressionPart(expr))
                ), scope
            )
            assertEquals(DataType.STRING, result.type)
            assertEquals("hello world test", result.stringValue)
        }

        @Test
        fun `builtin math functions contain non-number parts`() {
            val e = Executor()
            val expr = Expression(
                symbolPart("*"), listOf(
                    numericPart(1f),
                    numericPart(2f),
                    stringPart("test")
                )
            )

            assertThrows(RuntimeException::class.java) {
                e.execute(expr)
            }
        }

        private fun testBuiltin(op: String, vararg args: Float): Data {
            val e = Executor()
            val expr = Expression(symbolPart(op), args.map { numericPart(it) })
            return e.execute(expr)
        }

    }

    @Nested
    inner class KeywordTests {
        @Nested
        inner class IfExpressionTests {
            @Test
            fun `if expression with simple boolean and 2 simple parts`() {
                val executor = Executor()
                assertEquals(1f, executor.execute(simpleIfExpr(true, 1f, 2f)).numericValue)
                assertEquals(2f, executor.execute(simpleIfExpr(false, 1f, 2f)).numericValue)
            }

            @Test
            fun `if expression with complex boolean and 2 simple parts`() {
                val executor = Executor()
                val trueNegation = complexIfExpr(
                    simpleIfExpr(switch = true, truePart = false, falsePart = true),
                    1f, 2f
                )
                val falseNegation = complexIfExpr(
                    simpleIfExpr(switch = false, truePart = false, falsePart = true),
                    1f, 2f
                )

                assertEquals(2f, executor.execute(trueNegation).numericValue)
                assertEquals(1f, executor.execute(falseNegation).numericValue)
            }

            @Test
            fun `if expression with complex boolean and 2 complex parts`() {
                val executor = Executor()
                val switchExpr = simpleIfExpr(false, truePart = false, falsePart = true) // => true
                val trueExpr = simpleIfExpr(false, truePart = 1f, falsePart = 2f) // => 2f
                val falseExpr = simpleIfExpr(true, truePart = 10f, falsePart = 20f) // => 10f
                val composedExpr = complexIfExpr(switchExpr, trueExpr, falseExpr)

                assertEquals(2f, executor.execute(composedExpr).numericValue)
            }
        }

        @Nested
        inner class LetExpressionTests {
            @Test
            fun `let binding with one binding and simple body`() {
                val executor: Executor = Executor()
                val binding: Map<String, Float> = mapOf(Pair("x", 10f))
                val expr = makeLetBinding(binding, symbolPart("x"))
                assertEquals(10f, executor.execute(expr).numericValue)
            }

            @Test
            fun `let binding with two bindings and simple body`() {
                val executor: Executor = Executor()
                val binding: Map<String, Float> = mapOf(Pair("x", 10f), Pair("y", 100f))
                val expr = makeLetBinding(binding, symbolPart("y"))
                assertEquals(100f, executor.execute(expr).numericValue)
            }

            @Test
            fun `let binding with complex body`() {
                val executor: Executor = Executor()
                val binding: Map<String, Float> = mapOf(Pair("x", 10f), Pair("y", 100f))
                val bodyExpr = Expression(symbolPart("*"), listOf(symbolPart("x"), symbolPart("y")))
                val expr = makeLetBinding(binding, expressionPart(bodyExpr))
                assertEquals(1000f, executor.execute(expr).numericValue)
            }

            private fun makeLetBinding(bindings: Map<String, Float>, body: ExpressionPart): Expression {
                val bindingsAsParts = bindings.entries
                    .map { entry -> Expression(symbolPart(entry.key), listOf(numericPart(entry.value))) }
                    .map { e -> expressionPart(e) }
                val bindingExpr = Expression(bindingsAsParts.first(), bindingsAsParts.drop(1))
                return Expression(keywordPart(KeywordType.LET), listOf(expressionPart(bindingExpr), body))
            }
        }

        @Nested
        inner class FunExpressionsTests {
            private val executor = Executor()
            private val functionName = "f"
            private val params = expressionPart(Expression(symbolPart("a"), listOf(symbolPart("b"), symbolPart("c"))))
            private val bodyPart = expressionPart(formIfExpr(symbolPart("a"), symbolPart("b"), symbolPart("c")))
            private val expr = Expression(keywordPart(KeywordType.FUN), listOf(symbolPart(functionName), params, bodyPart))

            @Test
            fun `executes complex function and sets in scope`() {
                val scope = Scope()
                val executionExpr = Expression(symbolPart("f"),
                    listOf(booleanPart(false), numericPart(10f), numericPart(100f)))

                // Before definition, this should make a symbol lookup error
                assertThrows(ScopeDataException::class.java) {
                    executor.execute(executionExpr, scope)
                }

                // This should side-effect the calling scope
                val definitionResult = executor.execute(expr, scope)
                assertEquals(DataType.FUNCTION, definitionResult.type)
                assertNotNull(scope.lookup("f"))

                // Now the execution should pass
                val executionResult = executor.execute(executionExpr, scope)
                assertEquals(100f, executionResult.numericValue)
            }

            @Test
            fun `executes with returned function`() {
                val scope = Scope()
                val executionExpr = Expression(expressionPart(expr),
                    listOf(booleanPart(false), numericPart(10f), numericPart(100f)))
                val result = executor.execute(executionExpr, scope)

                assertNotNull(scope.lookup(functionName))
                assertEquals(100f, result.numericValue)
            }
        }
    }

    private fun complexIfExpr(switch: Expression, truePart: Expression, falsePart: Expression): Expression =
        formIfExpr(expressionPart(switch), expressionPart(truePart), expressionPart(falsePart))

    private fun complexIfExpr(switch: Expression, truePart: Float, falsePart: Float): Expression =
        formIfExpr(expressionPart(switch), numericPart(truePart), numericPart(falsePart))

    private fun simpleIfExpr(switch: Boolean, truePart: Boolean, falsePart: Boolean): Expression =
        formIfExpr(booleanPart(switch), booleanPart(truePart), booleanPart(falsePart))

    private fun simpleIfExpr(switch: Boolean, truePart: Float, falsePart: Float): Expression =
        formIfExpr(booleanPart(switch), numericPart(truePart), numericPart(falsePart))

    private fun formIfExpr(
        switchPart: ExpressionPart,
        truePart: ExpressionPart,
        falsePart: ExpressionPart
    ): Expression = Expression(keywordPart(KeywordType.IF), listOf(switchPart, truePart, falsePart))
}
