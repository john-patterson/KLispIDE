import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.expressions.Expression
import com.statelesscoder.klisp.compiler.types.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KLValueTests {
    @Nested
    inner class FunctionTests {
        @Test
        fun `throws if args do not match param list`() {
            val e = Executor()
            val f = Function("f", emptyList(), KLNumber(1f))

            assertThrows(RuntimeException::class.java) {
                f.run(e, RealizedList(listOf(KLString("g"))))
            }

            val g = Function("g", listOf(Symbol("a")), KLNumber(1f))
            assertThrows(RuntimeException::class.java) {
                g.run(e, RealizedList())
            }
        }

        @Test
        fun `function with 1 arg that is not used`() {
            val e = Executor()
            val f = Function("f", listOf(Symbol("a")), KLNumber(1f))
            val result = f.run(e, RealizedList(listOf(KLNumber(10f)))) as KLNumber

            assertEquals(1f, result.value)
        }

        @Test
        fun `function with 2 arg that are not used`() {
            val e = Executor()
            val params = listOf(Symbol("a"), Symbol("b"))
            val args = listOf(
                KLNumber(10f),
                KLNumber(20f)
            )
            val f = Function("f", params, KLNumber(1f))
            val result = f.run(e, RealizedList(args)) as KLNumber

            assertEquals(1f, result.value)
        }

        @Test
        fun `function which takes one parameter and uses it`() {
            val e = Executor()
            val params = listOf(Symbol("x"))
            val args = listOf(KLNumber(10f))
            val f = Function("f", params, Symbol("x"))
            val result = f.run(e, RealizedList(args)) as KLNumber

            assertEquals(10f, result.value)
        }

        @Test
        fun `function which takes two parameters and uses one`() {
            val e = Executor()
            val params = listOf(Symbol("x"), Symbol("y"))
            val args = listOf(
                KLNumber(10f),
                KLNumber(20f)
            )
            val f = Function("f", params, Symbol("x"))
            val result = f.run(e, RealizedList(args)) as KLNumber

            assertEquals(10f, result.value)
        }

        @Test
        fun `function which has a complex expr in the body`() {
            val scope = Scope()
            val e = Executor()
            // This is: (fun id (x) x) and
            val id = Function("id", listOf(Symbol("x")), Symbol("x"))
            scope.add(Symbol("id"), id)

            // This is: (fun f (a b) (id b))
            val params = listOf(Symbol("a"), Symbol("b"))
            val expr = Expression(
                Symbol("id"),
                listOf(Symbol("b"))
            )
            val f = Function("f", params, expr)
            val result = f.run(e, RealizedList(listOf(
                KLNumber(1f),
                KLNumber(2f)
            )), scope) as KLNumber

            assertEquals(2f, result.value)
        }

        @Test
        fun `toString simple types`() {
            assertEquals("1.0", KLNumber(1f).toString())
            assertEquals("\"foo\"", KLString("foo").toString())
            assertEquals("true", KLBool(true).toString())
        }

        @Test
        fun `toString function`() {
            val params = listOf(Symbol("a"), Symbol("b"))
            val body = Expression(
                    Symbol("+"),
                    listOf(Symbol("a"), Symbol("b")))
            val f = Function(Symbol("foo"), params, body)
            assertEquals("(fun foo [a b] (+ a b))", f.toString())
        }
    }
}

