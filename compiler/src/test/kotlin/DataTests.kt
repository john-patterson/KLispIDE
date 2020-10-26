import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.types.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataTests {
    @Nested
    inner class FunctionTests {
        @Test
        fun `throws if args do not match param list`() {
            val e = Executor()
            val f = Function(e, "f", emptyList(), numericPart(1f))

            assertThrows(RuntimeException::class.java) {
                f.run(listOf(createData("g")))
            }

            val g = Function(e, "g", listOf(symbolPart("a")), numericPart(1f))
            assertThrows(RuntimeException::class.java) {
                g.run(emptyList())
            }
        }

        @Test
        fun `function with 1 arg that is not used`() {
            val e = Executor()
            val f = Function(e, "f", listOf(symbolPart("a")), numericPart(1f))
            val result = f.run(listOf(createData(10f)))

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(1f, result.numericValue)
        }

        @Test
        fun `function with 2 arg that are not used`() {
            val e = Executor()
            val params = listOf(symbolPart("a"), symbolPart("b"))
            val args = listOf(
                createData(10f),
                createData(20f)
            )
            val f = Function(e, "f", params, numericPart(1f))
            val result = f.run(args)

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(1f, result.numericValue)
        }

        @Test
        fun `function which takes one parameter and uses it`() {
            val e = Executor()
            val params = listOf(symbolPart("x"))
            val args = listOf(createData(10f))
            val f = Function(e, "f", params, symbolPart("x"))
            val result = f.run(args)

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(10f, result.numericValue)
        }

        @Test
        fun `function which takes two parameters and uses one`() {
            val e = Executor()
            val params = listOf(symbolPart("x"), symbolPart("y"))
            val args = listOf(
                createData(10f),
                createData(20f)
            )
            val f = Function(e, "f", params, symbolPart("x"))
            val result = f.run(args)

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(10f, result.numericValue)
        }

        @Test
        fun `function which has a complex expr in the body`() {
            val scope = Scope()
            val e = Executor()
            // This is: (fun id (x) x) and
            val id = Function(e, "id", listOf(symbolPart("x")), symbolPart("x"))
            scope.add("id", createData(id))

            // This is: (fun f (a b) (id b))
            val params = listOf(symbolPart("a"), symbolPart("b"))
            val expr = Expression(
                symbolPart("id"),
                listOf(symbolPart("b"))
            )
            val f = Function(e, "f", params, expressionPart(expr))
            val result = f.run(listOf(
                createData(1f),
                createData(2f)
            ), scope)

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(2f, result.numericValue)
        }

        @Test
        fun `toString simple types`() {
            assertEquals("1.0", createData(1f).toString())
            assertEquals("\"foo\"", createData("foo").toString())
            assertEquals("true", createData(true).toString())
        }

        @Test
        fun `toString function`() {
            val e = Executor()
            val f = Function(e, "foo", listOf(symbolPart("a"), symbolPart("b")),
                expressionPart(
                    Expression(
                        symbolPart("+"),
                        listOf(symbolPart("a"), symbolPart("b"))
                    )
                ))
            assertEquals("(fun foo (a b) (+ a b))", f.toString())
        }
    }
}

