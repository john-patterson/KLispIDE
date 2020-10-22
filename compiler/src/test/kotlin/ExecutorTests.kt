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
            assertEquals(400f, testBuiltin("*", 10f, 20f, 2f).innerValue)
            assertEquals(32f, testBuiltin("+", 10f, 20f, 2f).innerValue)
            assertEquals(-12f, testBuiltin("-", 10f, 20f, 2f).innerValue)
            assertEquals(0.25f, testBuiltin("/", 10f, 20f, 2f).innerValue)
        }

        @Test
        fun `builtin functions work with complex types`() {
            val e = Executor()
            val expr = Expression(symbolPart("*"), listOf(
                expressionPart(Expression(symbolPart("+"), listOf(
                    numericPart(1f),
                    numericPart(2f)))),
                numericPart(10f)))
            assertEquals(30f, e.execute(expr).innerValue)
        }

        @Test
        fun `builtin print function concatenates strings`() {
            val e = Executor()
            val result = e.execute(Expression(symbolPart("pRINt"),
                listOf(stringPart("hello"), stringPart("world"))))
            assertEquals(DataType.STRING, result.data.type)
            assertEquals("hello world", result.innerText)
        }

        @Test
        fun `builtin print function fails on non-strings`() {
            val e = Executor()

            assertThrows(RuntimeException::class.java) {
                e.execute(Expression(symbolPart("pRINt"),
                    listOf(stringPart("hello"), stringPart("world"), numericPart(2f))))
            }
        }

        @Test
        fun `builtin print function succeeds on complex objects which evaluate to strings`() {
            val e = Executor()
            val scope = Scope()
            val id = Function(e, "id", listOf(symbolPart("x")), symbolPart("x"))
            scope.add("id", functionData(id))

            val expr = Expression(symbolPart("id"), listOf(stringPart("test")))
            val result = e.execute(Expression(symbolPart("pRINt"),
                listOf(stringPart("hello"), stringPart("world"), expressionPart(expr))), scope)
            assertEquals(DataType.STRING, result.data.type)
            assertEquals("hello world test", result.innerText)
        }

        @Test
        fun `builtin math functions contain non-number parts`() {
            val e = Executor()
            val expr = Expression(symbolPart("*"), listOf(
                numericPart(1f),
                numericPart(2f),
                stringPart("test")))

            assertThrows(RuntimeException::class.java) {
                e.execute(expr)
            }
        }

        private fun testBuiltin(op: String, vararg args: Float): ExecutionResult {
            val e = Executor()
            val expr = Expression(symbolPart(op), args.map { numericPart(it) })
            return e.execute(expr)
        }

    }

    @Nested
    inner class KeywordTests {
        @Test
        fun `if expression with simple boolean and 2 parts`() {
            val executor = Executor()
            val trueExpr = Expression(keywordPart(KeywordType.IF), listOf(booleanPart(true), numericPart(1f), numericPart(2f)))
            assertEquals(1f, executor.execute(trueExpr).innerValue)

            val falseExpr = Expression(keywordPart(KeywordType.IF), listOf(booleanPart(false), numericPart(1f), numericPart(2f)))
            assertEquals(2f, executor.execute(falseExpr).innerValue)
        }
    }
}
