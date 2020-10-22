import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.Function
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataTests {
    @Nested
    inner class UserDefinedFunctionTests {
        @Test
        fun `throws if args do not match param list`() {
            val e = Executor()
            val f = UserDefinedFunction(e, "f", emptyList(), numericPart(1f))

            assertThrows(RuntimeException::class.java) {
                f.run(listOf(stringData("g")))
            }

            val g = UserDefinedFunction(e, "g", listOf(symbolPart("a")), numericPart(1f))
            assertThrows(RuntimeException::class.java) {
                g.run(emptyList())
            }
        }

        @Test
        fun `function with 1 arg that is not used`() {
            val e = Executor()
            val f = UserDefinedFunction(e, "f", listOf(symbolPart("a")), numericPart(1f))
            val result = f.run(listOf(numericData(10f)))

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(1f, result.numericValue)
        }

        @Test
        fun `function with 2 arg that are not used`() {
            val e = Executor()
            val params = listOf(symbolPart("a"), symbolPart("b"))
            val args = listOf(numericData(10f), numericData(20f))
            val f = UserDefinedFunction(e, "f", params, numericPart(1f))
            val result = f.run(args)

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(1f, result.numericValue)
        }

        @Test
        fun `function which takes one parameter and uses it`() {
            val e = Executor()
            val params = listOf(symbolPart("x"))
            val args = listOf(numericData(10f))
            val f = UserDefinedFunction(e, "f", params, symbolPart("x"))
            val result = f.run(args)

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(10f, result.numericValue)
        }

        @Test
        fun `function which takes two parameters and uses one`() {
            val e = Executor()
            val params = listOf(symbolPart("x"), symbolPart("y"))
            val args = listOf(numericData(10f), numericData(20))
            val f = UserDefinedFunction(e, "f", params, symbolPart("x"))
            val result = f.run(args)

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(10f, result.numericValue)
        }

        @Test
        fun `function which has a complex expr in the body`() {
            val scope = Scope()
            val e = Executor()
            // This is: (fun id (x) x) and
            val id = UserDefinedFunction(e, "id", listOf(symbolPart("x")), symbolPart("x"))
            scope.add("id", functionData(id))

            // This is: (fun f (a b) (id b))
            val params = listOf(symbolPart("a"), symbolPart("b"))
            val expr = Expression(symbolPart("id"), listOf(symbolPart("b")))
            val f = UserDefinedFunction(e, "f", params, expressionPart(expr))
            val result = f.run(listOf(numericData(1f), numericData(2f)), scope)

            assertEquals(DataType.NUMBER, result.type)
            assertEquals(2f, result.numericValue)
        }
    }

    @Nested
    inner class BuiltinFunctionTests {

    }
}

