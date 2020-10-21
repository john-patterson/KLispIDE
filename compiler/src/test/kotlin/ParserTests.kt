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
                Parser().parse(listOf(identifierToken("foo", 0), rightParensToken( 3)))
            }
        }

        @Test
        fun `throws when expr head is a number`() {
            assertThrows(ParsingException::class.java) {
                Parser().parse(listOf(
                    leftParensToken(0),
                    numericToken("1", 1),
                    numericToken("2", 2),
                    rightParensToken(3)
                ))
            }
        }

        @Test
        fun `parses function with no args`() {
            val result = Parser().parse(listOf(
                leftParensToken(0),
                identifierToken("fOo", 1),
                rightParensToken(4)))
            assertEquals(ExpressionPartType.SYMBOL, result.head.type)
            assertIsSymbol("fOo", result.head)
            assertTrue(result.tail.isEmpty())
        }

        @Test
        fun `parses function tail full of simple things`() {
            val result = Parser().parse(listOf(
                leftParensToken(0),
                identifierToken("f", 1),
                numericToken("123", 2),
                booleanToken("true", 5),
                identifierToken("foo", 9),
                rightParensToken(12)))

            assertIsSymbol("f", result.head)
            assertEquals(3, result.tail.size)
            assertIsNumber(123.0f, result.tail[0])
            assertIsBoolean(true, result.tail[1])
            assertIsSymbol("foo", result.tail[2])
        }

        @Test
        fun `parses function with expression in tail`() {
            val result = Parser().parse(listOf(
                leftParensToken(0),
                    identifierToken("f",1),
                    leftParensToken(2),
                        identifierToken("g", 3),
                        numericToken("1", 4),
                    rightParensToken(5),
                    numericToken("2", 6),
                rightParensToken(7)))

            assertIsSymbol("f", result.head)
            assertEquals(2, result.tail.size)
            assertIsExpression({e ->
                assertIsSymbol("g", e.head)
                assertIsNumber(1.0f, e.tail[0])
            }, result.tail[0])
            assertIsNumber(2.0f, result.tail[1])
        }

        @Test
        fun `parses function with expression in head`() {
            val result = Parser().parse(listOf(
                leftParensToken(0),
                    leftParensToken(1),
                        identifierToken("f", 2),
                        numericToken("1", 3),
                    rightParensToken(4),
                numericToken("2", 5),
                rightParensToken(6)))

            assertIsExpression({e ->
                assertIsSymbol("f", e.head)
                assertEquals(1, e.tail.size)
                assertIsNumber(1.0f, e.tail[0])
            }, result.head)
            assertEquals(1, result.tail.size)
            assertIsNumber(2.0f, result.tail[0])
        }
    }

    @Nested
    inner class LetBinding {
        @Test
        fun `one let binding in a simple environment`() {
            val tokenizer = Tokenizer()
            val parser = Parser()
            val tokens = tokenizer.scan("(let ((a 1)) (f a))")

            val result = parser.parse(tokens)
            assertIsKeyword(KeywordType.LET, result.head)

            assertIsExpression({ expr ->
                assertIsExpression({ bind ->
                    assertIsSymbol("a", bind.head)
                    assertIsNumber(1.0f, bind.tail[0])
                }, expr.head)
            }, result.tail[0])

            assertIsExpression({ expr ->
                assertIsSymbol("f", expr.head)
                assertIsSymbol("a", expr.tail[0])
            }, result.tail[1])
        }

        @Test
        fun `let binding with multiple bindings`() {
            val tokenizer = Tokenizer()
            val parser = Parser()
            val tokens = tokenizer.scan("(let ((a 1) (b 2)) (f a b))")

            val result = parser.parse(tokens)
            assertIsKeyword(KeywordType.LET, result.head)

            assertIsExpression({ expr ->
                assertIsExpression({ bind ->
                    assertIsSymbol("a", bind.head)
                    assertIsNumber(1.0f, bind.tail[0])
                }, expr.head)
                assertIsExpression({ bind ->
                    assertIsSymbol("b", bind.head)
                    assertIsNumber(2.0f, bind.tail[0])
                }, expr.tail[0])
            }, result.tail[0])

            assertIsExpression({ expr ->
                assertIsSymbol("f", expr.head)
                assertIsSymbol("a", expr.tail[0])
                assertIsSymbol("b", expr.tail[1])
            }, result.tail[1])
        }

        @Test
        fun `empty let binding throws`() {
            val tokenizer = Tokenizer()
            val parser = Parser()
            val tokens = tokenizer.scan("(let () (f 1))")

            assertThrows(ParsingException::class.java) {
                parser.parse(tokens)
            }
        }

        @Test
        fun `empty let environment throws`() {
            val tokenizer = Tokenizer()
            val parser = Parser()
            val tokens = tokenizer.scan("(let ((a 1)) ())")

            assertThrows(ParsingException::class.java) {
                parser.parse(tokens)
            }
        }
    }

    fun assertIsExpression(expressionAssertion: (Expression) -> Unit, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.EXPRESSION, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNotNull(actual.expression)
        expressionAssertion(actual.expression!!) // Value verified by previous line
        assertNull(actual.keywordType)
    }

    fun assertIsNumber(expected: Float, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.NUMBER, actual.type)
        assertNotNull(actual.value)
        assertEquals(expected, actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
        assertNull(actual.keywordType)
    }

    fun assertIsBoolean(expected: Boolean, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.BOOLEAN, actual.type)
        assertNull(actual.value)
        assertNotNull(actual.truth)
        assertEquals(expected, actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
        assertNull(actual.keywordType)
    }

    fun assertIsSymbol(expected: String, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.SYMBOL, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNotNull(actual.name)
        assertEquals(expected, actual.name)
        assertNull(actual.expression)
        assertNull(actual.keywordType)
    }

    fun assertIsKeyword(expected: KeywordType, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.KEYWORD, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
        assertNotNull(actual.keywordType)
        assertEquals(expected, actual.keywordType)
    }
}
