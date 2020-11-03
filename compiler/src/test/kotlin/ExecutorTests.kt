import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.UserDefinedFunction
import com.statelesscoder.klisp.compiler.exceptions.ScopeDataException
import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.expressions.*
import com.statelesscoder.klisp.compiler.types.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExecutorTests {
    @Nested
    inner class BuiltinTests {
        @Test
        fun `car returns the head of a list`() {
            val e = Executor()
            val list = RealizedList(listOf(KLNumber(1f), KLNumber(2f)))
            val expr = Expression(
                Symbol("car"),
                listOf(list))
            val result = e.execute(expr) as KLNumber
            assertEquals(1f, result.value)
        }

        @Test
        fun `car on an empty list fails`() {
            val e = Executor()
            val list = RealizedList()
            val expr = Expression(
                Symbol("car"),
                listOf(list))

            assertThrows(RuntimeException::class.java) {
                e.execute(expr)
            }
        }

        @Test
        fun `cdr returns the tail of a list`() {
            val e = Executor()
            val list = RealizedList(listOf(KLNumber(1f), KLNumber(2f)))
            val expr = Expression(
                Symbol("cdr"),
                listOf(list))
            val result = e.execute(expr)
            assertEquals(2f, ((result as RealizedList).items[0] as KLNumber).value)
        }

        @Test
        fun `cdr on an empty list fails`() {
            val e = Executor()
            val list = RealizedList()
            val expr = Expression(
                Symbol("cdr"),
                listOf(list))

            assertThrows(RuntimeException::class.java) {
                e.execute(expr)
            }
        }

        @Test
        fun `cons adds items to list`() {
            val e = Executor()
            val list = RealizedList()
            val innerExpr = Expression(Symbol("cons"),
                listOf(list, KLNumber(1f)))
            val expr = Expression(
                Symbol("cons"),
                listOf(innerExpr, KLNumber(2f)))
            val result = e.execute(expr)
            val resultAsList = result as RealizedList
            assertEquals(2, resultAsList.items.size)
            assertEquals(1f, (resultAsList.items[0] as KLNumber).value)
            assertEquals(2f, (resultAsList.items[1] as KLNumber).value)
        }

        @Test
        fun `logic functions work`() {
            fun testLogicFunction(func: String, expected: Boolean, vararg args: Boolean) {
                val e = Executor()
                val expr = Expression(Symbol(func), args.map { KLBool(it) })
                val result = e.execute(expr) as KLBool
                assertEquals(expected, result.truth)
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

                val equalExpr = Expression(Symbol("eq"), parts.asList())
                val equalResult = (e.execute(equalExpr) as KLBool).truth
                val notEqualExpr = Expression(Symbol("neq"), parts.asList())
                val notEqualResult = (e.execute(notEqualExpr) as KLBool).truth
                assertEquals(areEqual, equalResult)
                assertNotEquals(areEqual, notEqualResult)
            }

            testEqualityFunction(true, KLNumber(1f), KLNumber(1f))
            testEqualityFunction(false, KLNumber(1f), KLString("1"))
            testEqualityFunction(false, KLNumber(1f), KLNumber(2f))
            testEqualityFunction(false, KLString("1"), KLString("2"))
            testEqualityFunction(true, KLString("1"), KLString("1"))
            testEqualityFunction(true, KLBool(true), KLBool(true))
            testEqualityFunction(false, KLBool(true), KLBool(false))

            val list1 = RealizedList(listOf(KLNumber(1f), KLNumber(2f), KLNumber(3f)))
            val list2 = RealizedList(listOf(KLNumber(1f), KLNumber(2f), KLNumber(3f)))
            val list3 = RealizedList(listOf(KLNumber(1f)))
            val emptylist = RealizedList()
            testEqualityFunction(true, list1, list1)
            testEqualityFunction(true, list1, list2)
            testEqualityFunction(false, list1, list3)
            testEqualityFunction(false, emptylist, list3)
            testEqualityFunction(true, emptylist, emptylist)
        }

        @Test
        fun `builtin functions work with simple types`() {
            assertEquals(400f, testBuiltin("*", 10f, 20f, 2f).value)
            assertEquals(32f, testBuiltin("+", 10f, 20f, 2f).value)
            assertEquals(-12f, testBuiltin("-", 10f, 20f, 2f).value)
            assertEquals(0.25f, testBuiltin("/", 10f, 20f, 2f).value)
        }

        @Test
        fun `builtin functions work with complex types`() {
            val e = Executor()
            val expr = Expression(
                Symbol("*"), listOf(
                    Expression(
                        Symbol("+"), listOf(
                            KLNumber(1f),
                            KLNumber(2f)
                        )
                    ),
                    KLNumber(10f)
                )
            )
            assertEquals(30f, (e.execute(expr) as KLNumber).value)
        }

        @Test
        fun `builtin print function concatenates strings`() {
            val e = Executor()
            val result = e.execute(
                Expression(
                    Symbol("pRINt"),
                    listOf(KLString("hello"), KLString("world"))
                )
            ) as KLString
            assertEquals("hello world", result.text)
        }

        @Test
        fun `builtin print function fails on non-strings`() {
            val e = Executor()

            assertThrows(RuntimeException::class.java) {
                e.execute(
                    Expression(
                        Symbol("pRINt"),
                        listOf(KLString("hello"), KLString("world"), KLNumber(2f))
                    )
                )
            }
        }

        @Test
        fun `builtin print function succeeds on complex objects which evaluate to strings`() {
            val e = Executor()
            val scope = Scope()
            val id = UserDefinedFunction("id", listOf(Symbol("x")), Symbol("x"), scope)
            scope.add(Symbol("id"), id)

            val expr = Expression(
                Symbol("id"),
                listOf(KLString("test"))
            )
            val result = e.execute(
                Expression(
                    Symbol("pRINt"),
                    listOf(KLString("hello"), KLString("world"), expr)
                ), scope
            ) as KLString
            assertEquals("hello world test", result.text)
        }

        @Test
        fun `builtin math functions contain non-number parts`() {
            val e = Executor()
            val expr = Expression(
                Symbol("*"), listOf(
                    KLNumber(1f),
                    KLNumber(2f),
                    KLString("test")
                )
            )

            assertThrows(RuntimeException::class.java) {
                e.execute(expr)
            }
        }

        private fun testBuiltin(op: String, vararg args: Float): KLNumber {
            val e = Executor()
            val expr = Expression(
                Symbol(op),
                args.map { KLNumber(it) })
            return e.execute(expr) as KLNumber
        }

    }

    @Nested
    inner class SimpleKLValueTests {
        private val e = Executor()

        @Test
        fun `can return number`() {
            val scope = Scope()
            defineConstantFunction(KLNumber(1f), scope)
            val result = callConstantFunction(scope) as KLNumber
            assertEquals(1f, result.value)
        }

        @Test
        fun `can return string`() {
            val scope = Scope()
            defineConstantFunction(KLString("hey"), scope)
            val result = callConstantFunction(scope) as KLString
            assertEquals("hey", result.text)
        }

        @Test
        fun `can return boolean`() {
            val scope = Scope()
            defineConstantFunction(KLBool(true), scope)
            val result = callConstantFunction(scope) as KLBool
            assertEquals(true, result.truth)
        }

        @Test
        fun `can return list`() {
            val scope = Scope()
            val klist = RealizedList(listOf(KLString("a"), KLNumber(1f)))
            defineConstantFunction(klist, scope)
            val result = callConstantFunction(scope)
            val resultAsList = result as RealizedList
            assertEquals(2, resultAsList.items.size)
            assertEquals("a", (resultAsList.items[0] as KLString).text)
            assertEquals(1f, (resultAsList.items[1] as KLNumber).value)
        }

        private fun defineConstantFunction(returnValue: ExpressionPart, scope: Scope) {
            val definition = FunctionDefinition(Symbol("foo"), emptyList(), returnValue)
            definition.execute(e, scope)
        }

        private fun callConstantFunction(scope: Scope): KLValue {
            val functionCall = Expression(Symbol("foo"), emptyList())
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
                assertEquals(1f, (executor.execute(simpleIfExpr(true, 1f, 2f)) as KLNumber).value)
                assertEquals(2f, (executor.execute(simpleIfExpr(false, 1f, 2f)) as KLNumber).value)
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

                assertEquals(2f, (executor.execute(trueNegation) as KLNumber).value)
                assertEquals(1f, (executor.execute(falseNegation) as KLNumber).value)
            }

            @Test
            fun `if expression with complex boolean and 2 complex parts`() {
                val executor = Executor()
                val switchExpr = simpleIfExpr(false, truePart = false, falsePart = true) // => true
                val trueExpr = simpleIfExpr(false, truePart = 1f, falsePart = 2f) // => 2f
                val falseExpr = simpleIfExpr(true, truePart = 10f, falsePart = 20f) // => 10f
                val composedExpr = complexIfExpr(switchExpr, trueExpr, falseExpr)

                assertEquals(2f, (executor.execute(composedExpr) as KLNumber).value)
            }
        }

        @Nested
        inner class LetExpressionTests {
            @Test
            fun `let binding with one binding and simple body`() {
                val executor = Executor()
                val binding: Map<String, Float> = mapOf(Pair("x", 10f))
                val expr = makeLetBinding(binding, Symbol("x"))
                assertEquals(10f, (executor.execute(expr) as KLNumber).value)
            }

            @Test
            fun `let binding with two bindings and simple body`() {
                val executor = Executor()
                val binding: Map<String, Float> = mapOf(Pair("x", 10f), Pair("y", 100f))
                val expr = makeLetBinding(binding, Symbol("y"))
                assertEquals(100f, (executor.execute(expr) as KLNumber).value)
            }

            @Test
            fun `let binding with complex body`() {
                val executor = Executor()
                val binding: Map<String, Float> = mapOf(Pair("x", 10f), Pair("y", 100f))
                val bodyExpr = Expression(
                    Symbol("*"),
                    listOf(Symbol("x"), Symbol("y"))
                )
                val expr = makeLetBinding(binding, bodyExpr)
                assertEquals(1000f, (executor.execute(expr) as KLNumber).value)
            }

            private fun makeLetBinding(bindings: Map<String, Float>, body: ExpressionPart): Expression {
                val bindingsAsParts = bindings.entries
                    .map { entry ->
                        Expression(
                            Symbol(
                                entry.key
                            ), listOf(KLNumber(entry.value))
                        )
                    }
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
            private val params = listOf(Symbol("a"),
                            Symbol("b"),
                            Symbol("c"))

            private val bodyPart = IfExpression(Symbol("a"), Symbol("b"), Symbol("c"))
            private val expr = FunctionDefinition(Symbol(functionName), params, bodyPart)

            @Test
            fun `executes complex function and sets in scope`() {
                val scope = Scope()
                val executionExpr = Expression(
                    Symbol("f"),
                    listOf(KLBool(false), KLNumber(10f), KLNumber(100f))
                )

                // Before definition, this should make a symbol lookup error
                assertThrows(ScopeDataException::class.java) {
                    executor.execute(executionExpr, scope)
                }

                // This should side-effect the calling scope
                executor.execute(expr, scope) is UserDefinedFunction
                assertNotNull(scope.lookup(Symbol("f")))

                // Now the execution should pass
                val executionResult = executor.execute(executionExpr, scope) as KLNumber
                assertEquals(100f, executionResult.value)
            }

            @Test
            fun `executes with returned function`() {
                val scope = Scope()
                val executionExpr = Expression(
                    expr,
                    listOf(KLBool(false), KLNumber(10f), KLNumber(100f))
                )
                val result = executor.execute(executionExpr, scope) as KLNumber

                assertNotNull(scope.lookup(Symbol(functionName)))
                assertEquals(100f, result.value)
            }
        }
    }

    private fun complexIfExpr(switch: Expression, truePart: Expression, falsePart: Expression): IfExpression =
        IfExpression(switch, truePart, falsePart)

    private fun complexIfExpr(switch: Expression, truePart: Float, falsePart: Float): IfExpression =
        IfExpression(switch, KLNumber(truePart), KLNumber(falsePart))

    private fun simpleIfExpr(switch: Boolean, truePart: Boolean, falsePart: Boolean): IfExpression =
        IfExpression(KLBool(switch), KLBool(truePart), KLBool(falsePart))

    private fun simpleIfExpr(switch: Boolean, truePart: Float, falsePart: Float): IfExpression =
        IfExpression(KLBool(switch), KLNumber(truePart), KLNumber(falsePart))
}
