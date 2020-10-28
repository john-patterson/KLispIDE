import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.exceptions.ScopeDataException
import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.types.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.math.exp

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExecutorTests {
    @Nested
    inner class BuiltinTests {
        @Test
        fun `car returns the head of a list`() {
            val e = Executor()
            val list = KList(listOf(numericPart(1f), numericPart(2f)))
            val expr = Expression(
                symbolPart("car"),
                listOf(listPart(list)))
            val result = e.execute(expr)
            assertEquals(DataType.NUMBER, result.type)
            assertEquals(1f, result.numericValue)
        }

        @Test
        fun `car on an empty list fails`() {
            val e = Executor()
            val list = KList(emptyList())
            val expr = Expression(
                symbolPart("car"),
                listOf(listPart(list)))

            assertThrows(RuntimeException::class.java) {
                e.execute(expr)
            }
        }

        @Test
        fun `cdr returns the tail of a list`() {
            val e = Executor()
            val list = KList(listOf(numericPart(1f), numericPart(2f)))
            val expr = Expression(
                symbolPart("cdr"),
                listOf(listPart(list)))
            val result = e.execute(expr)
            assertEquals(DataType.LIST, result.type)
            assertEquals(2f, result.listValue!!.realizedData[0].numericValue)
        }

        @Test
        fun `cdr on an empty list fails`() {
            val e = Executor()
            val list = KList(emptyList())
            val expr = Expression(
                symbolPart("cdr"),
                listOf(listPart(list)))

            assertThrows(RuntimeException::class.java) {
                e.execute(expr)
            }
        }

        @Test
        fun `cons adds items to list`() {
            val e = Executor()
            val list = KList(emptyList())
            val innerExpr = Expression(symbolPart("cons"),
                listOf(listPart(list), numericPart(1f)))
            val expr = Expression(
                symbolPart("cons"),
                listOf(expressionPart(innerExpr), numericPart(2f)))
            val result = e.execute(expr)
            assertEquals(DataType.LIST, result.type)
            assertEquals(2, result.listValue!!.realizedData.size)
            assertEquals(1f, result.listValue!!.realizedData[0].numericValue)
            assertEquals(2f, result.listValue!!.realizedData[1].numericValue)
        }

        @Test
        fun `logic functions work`() {
            fun testLogicFunction(func: String, expected: Boolean, vararg args: Boolean) {
                val e = Executor()
                val expr = Expression(symbolPart(func), args.map { booleanPart(it) })
                val result = e.execute(expr)
                assertEquals(expected, result.truthyValue)
            }

            testLogicFunction("and", true, true, true, true)
            testLogicFunction("and", false, true, true, false)
            testLogicFunction("and", false, true, false, true)
            testLogicFunction("and", false, false, false, false)
            testLogicFunction("or", true, true, true)
            testLogicFunction("or", true, true, false)
            testLogicFunction("or", true, false, true)
            testLogicFunction("or", false, false, false)
            testLogicFunction("not", true, false)
            testLogicFunction("not", false, true)
        }

        @Test
        fun `equality functions work`() {
            fun testEqualityFunction(areEqual: Boolean, vararg parts: ExpressionPart) {
                val e = Executor()

                val equalExpr = Expression(symbolPart("eq"), parts.asList())
                val equalResult = e.execute(equalExpr).truthyValue
                val notEqualExpr = Expression(symbolPart("neq"), parts.asList())
                val notEqualResult = e.execute(notEqualExpr).truthyValue
                assertEquals(areEqual, equalResult)
                assertNotEquals(areEqual, notEqualResult)
            }

            testEqualityFunction(true, numericPart(1f), numericPart(1f))
            testEqualityFunction(false, numericPart(1f), stringPart("1"))
            testEqualityFunction(false, numericPart(1f), numericPart(2f))
            testEqualityFunction(false, stringPart("1"), stringPart("2"))
            testEqualityFunction(true, stringPart("1"), stringPart("1"))
            testEqualityFunction(true, booleanPart(true), booleanPart(true))
            testEqualityFunction(false, booleanPart(true), booleanPart(false))

            val list1 = listPart(KList(listOf(numericPart(1f), numericPart(2f), numericPart(3f))))
            val list2 = listPart(KList(listOf(numericPart(1f), numericPart(2f), numericPart(3f))))
            val list3 = listPart(KList(listOf(numericPart(1f))))
            val emptylist = listPart(KList(emptyList()))
            testEqualityFunction(true, list1, list1)
            testEqualityFunction(true, list1, list2)
            testEqualityFunction(false, list1, list3)
            testEqualityFunction(false, emptylist, list3)
            testEqualityFunction(true, emptylist, emptylist)
        }

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

            val expr = Expression(
                symbolPart("id"),
                listOf(stringPart("test"))
            )
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
            val expr = Expression(
                symbolPart(op),
                args.map { numericPart(it) })
            return e.execute(expr)
        }

    }

    @Nested
    inner class SimpleDataTests {
        private val e = Executor()

        @Test
        fun `can return number`() {
            val scope = Scope()
            defineConstantFunction(numericPart(1f), scope)
            val result = callConstantFunction(scope)
            assertEquals(1f, result.numericValue)
        }

        @Test
        fun `can return string`() {
            val scope = Scope()
            defineConstantFunction(stringPart("hey"), scope)
            val result = callConstantFunction(scope)
            assertEquals("hey", result.stringValue)
        }

        @Test
        fun `can return boolean`() {
            val scope = Scope()
            defineConstantFunction(booleanPart(true), scope)
            val result = callConstantFunction(scope)
            assertEquals(true, result.truthyValue)
        }

        @Test
        fun `can return list`() {
            val scope = Scope()
            val klist = KList(listOf(stringPart("a"), numericPart(1f)))
            defineConstantFunction(listPart(klist), scope)
            val result = callConstantFunction(scope)
            assertEquals(2, result.listValue!!.realizedData.size)
            assertEquals("a", result.listValue!!.realizedData[0].stringValue)
            assertEquals(1f, result.listValue!!.realizedData[1].numericValue)
        }

        private fun defineConstantFunction(returnValue: ExpressionPart, scope: Scope) {
            val definition = Expression(keywordPart(KeywordType.FUN), listOf(
                symbolPart("foo"),
                returnValue
            ))
            e.execute(definition, scope)
        }

        private fun callConstantFunction(scope: Scope): Data {
            val functionCall = Expression(symbolPart("foo"), emptyList())
            return e.execute(functionCall, scope)
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
                val bodyExpr = Expression(
                    symbolPart("*"),
                    listOf(symbolPart("x"), symbolPart("y"))
                )
                val expr = makeLetBinding(binding, expressionPart(bodyExpr))
                assertEquals(1000f, executor.execute(expr).numericValue)
            }

            private fun makeLetBinding(bindings: Map<String, Float>, body: ExpressionPart): Expression {
                val bindingsAsParts = bindings.entries
                    .map { entry ->
                        Expression(
                            symbolPart(
                                entry.key
                            ), listOf(numericPart(entry.value))
                        )
                    }
                    .map { e -> expressionPart(e) }
                val bindingExpr = Expression(
                    bindingsAsParts.first(),
                    bindingsAsParts.drop(1)
                )
                return LetBinding(
                    bindingExpr,
                    body
                )
            }
        }

        @Nested
        inner class FunExpressionsTests {
            private val executor = Executor()
            private val functionName = "f"
            private val params = listPart(
                    KList(
                        listOf(symbolPart("a"), symbolPart("b"), symbolPart("c"))
                    )
                )

            private val bodyPart = expressionPart(IfExpression(symbolPart("a"), symbolPart("b"), symbolPart("c")))
            private val expr = Expression(
                keywordPart(KeywordType.FUN),
                listOf(symbolPart(functionName), params, bodyPart)
            )

            @Test
            fun `executes complex function and sets in scope`() {
                val scope = Scope()
                val executionExpr = Expression(
                    symbolPart("f"),
                    listOf(booleanPart(false), numericPart(10f), numericPart(100f))
                )

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
                val executionExpr = Expression(
                    expressionPart(expr),
                    listOf(booleanPart(false), numericPart(10f), numericPart(100f))
                )
                val result = executor.execute(executionExpr, scope)

                assertNotNull(scope.lookup(functionName))
                assertEquals(100f, result.numericValue)
            }
        }
    }

    private fun complexIfExpr(switch: Expression, truePart: Expression, falsePart: Expression): IfExpression =
        IfExpression(expressionPart(switch), expressionPart(truePart), expressionPart(falsePart))

    private fun complexIfExpr(switch: Expression, truePart: Float, falsePart: Float): IfExpression =
        IfExpression(expressionPart(switch), numericPart(truePart), numericPart(falsePart))

    private fun simpleIfExpr(switch: Boolean, truePart: Boolean, falsePart: Boolean): IfExpression =
        IfExpression(booleanPart(switch), booleanPart(truePart), booleanPart(falsePart))

    private fun simpleIfExpr(switch: Boolean, truePart: Float, falsePart: Float): IfExpression =
        IfExpression(booleanPart(switch), numericPart(truePart), numericPart(falsePart))
}
