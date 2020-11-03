import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.UserDefinedFunction
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
    inner class UserDefinedFunctionTests {
        @Test
        fun `throws if args do not match param list`() {
            val e = Executor()
            val f = UserDefinedFunction("f", emptyList(), KLNumber(1f), Scope())

            assertThrows(RuntimeException::class.java) {
                f.run(e, RealizedList(listOf(KLString("g"))))
            }

            val g = UserDefinedFunction("g", listOf(Symbol("a")), KLNumber(1f), Scope())
            assertThrows(RuntimeException::class.java) {
                g.run(e, RealizedList())
            }
        }

        @Test
        fun `function with 1 arg that is not used`() {
            val e = Executor()
            val f = UserDefinedFunction("f", listOf(Symbol("a")), KLNumber(1f), Scope())
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
            val f = UserDefinedFunction("f", params, KLNumber(1f), Scope())
            val result = f.run(e, RealizedList(args)) as KLNumber

            assertEquals(1f, result.value)
        }

        @Test
        fun `function which takes one parameter and uses it`() {
            val e = Executor()
            val params = listOf(Symbol("x"))
            val args = listOf(KLNumber(10f))
            val f = UserDefinedFunction("f", params, Symbol("x"), Scope())
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
            val f = UserDefinedFunction("f", params, Symbol("x"), Scope())
            val result = f.run(e, RealizedList(args)) as KLNumber

            assertEquals(10f, result.value)
        }

        @Test
        fun `function which has a complex expr in the body`() {
            val scope = Scope()
            val e = Executor()
            // This is: (fun id (x) x) and
            val id = UserDefinedFunction("id", listOf(Symbol("x")), Symbol("x"), Scope())
            scope.add(Symbol("id"), id)

            // This is: (fun f (a b) (id b))
            val params = listOf(Symbol("a"), Symbol("b"))
            val expr = Expression(
                Symbol("id"),
                listOf(Symbol("b"))
            )
            val f = UserDefinedFunction("f", params, expr, Scope())
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
            val f = UserDefinedFunction(Symbol("foo"), params, body, Scope())
            assertEquals("(fun foo [a b] (+ a b))", f.toString())
        }
    }
}

