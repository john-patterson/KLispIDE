import com.statelesscoder.klisp.compiler.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.Exception
import kotlin.math.exp

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParserTests {
    @Nested
    inner class FunctionInvocation {
        @Test
        fun `throws when expression isn't started by (`() {
            assertThrows(ParsingException::class.java) {
                Parser().parse(listOf(identifierToken("foo"), rightParensToken()))
            }
        }

        @Test
        fun `parses function with identifier leading`() {
            val result = Parser().parse(listOf(leftParensToken(), identifierToken("fOo"), rightParensToken()))
            assertEquals(ExpressionPartType.SYMBOL, result.head.type)
            assertIsSymbol("fOo", result.head)
            assertTrue(result.tail.isEmpty())
        }

        @Test
        fun `parses function tail full of simple things`() {
            val result = Parser().parse(listOf(
                leftParensToken(),
                identifierToken("f"),
                numericToken("123"),
                booleanToken("true"),
                identifierToken("foo"),
                rightParensToken()))

            assertIsSymbol("f", result.head)
            assertEquals(3, result.tail.size)
            assertIsNumber(123.0f, result.tail[0])
            assertIsBoolean(true, result.tail[1])
            assertIsSymbol("foo", result.tail[2])
        }

        @Test
        fun `parses function with expression in tail`() {
            val result = Parser().parse(listOf(
                leftParensToken(),
                    identifierToken("f"),
                    leftParensToken(),
                        identifierToken("g"),
                        numericToken("1"),
                    rightParensToken(),
                    numericToken("2"),
                rightParensToken()))

            assertIsSymbol("f", result.head)
            assertEquals(2, result.tail.size)
            assertIsExpression({e ->
                assertIsSymbol("g", e.head)
                assertIsNumber(1.0f, e.tail[0])
            }, result.tail[0])
            assertIsNumber(2.0f, result.tail[1])
        }
    }

    fun assertIsExpression(expressionAssertion: (Expression) -> Unit, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.EXPRESSION, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNotNull(actual.expression)
        expressionAssertion(actual.expression!!) // Value verified by previous line
    }

    fun assertIsNumber(expected: Float, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.NUMBER, actual.type)
        assertNotNull(actual.value)
        assertEquals(expected, actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
    }

    fun assertIsBoolean(expected: Boolean, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.BOOLEAN, actual.type)
        assertNull(actual.value)
        assertNotNull(actual.truth)
        assertEquals(expected, actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
    }


    fun assertIsSymbol(expected: String, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.SYMBOL, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNotNull(actual.name)
        assertEquals(expected, actual.name)
        assertNull(actual.expression)
    }
}
