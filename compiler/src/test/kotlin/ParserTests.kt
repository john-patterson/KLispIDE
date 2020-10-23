import com.statelesscoder.klisp.compiler.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParserTests {
    @Nested
    inner class FunctionInvocation {
        @Test
        fun `throws when expression isn't started by (`() {
            assertThrows(ParsingException::class.java) {
                Parser().parseSingleExpression(listOf(identifierToken("foo", 0), rightParensToken( 3)))
            }
        }

        @Test
        fun `throws when expr head is a number`() {
            assertThrows(ParsingException::class.java) {
                Parser().parseSingleExpression(listOf(
                    leftParensToken(0),
                    numericToken("1", 1),
                    numericToken("2", 2),
                    rightParensToken(3)
                ))
            }
        }

        @Test
        fun `parses function with no args`() {
            val result = Parser().parseSingleExpression(listOf(
                leftParensToken(0),
                identifierToken("fOo", 1),
                rightParensToken(4)))
            assertEquals(ExpressionPartType.SYMBOL, result.head.type)
            assertIsSymbol("fOo", result.head)
            assertTrue(result.tail.isEmpty())
        }

        @Test
        fun `parses function tail full of simple things`() {
            val result = Parser().parseSingleExpression(listOf(
                leftParensToken(0),
                identifierToken("f", 1),
                numericToken("123", 2),
                booleanToken("true", 5),
                identifierToken("foo", 9),
                stringToken("\"s\"", 12),
                rightParensToken(15)))

            assertIsSymbol("f", result.head)
            assertEquals(4, result.tail.size)
            assertIsNumber(123.0f, result.tail[0])
            assertIsBoolean(true, result.tail[1])
            assertIsSymbol("foo", result.tail[2])
            assertIsString("s", result.tail[3])
        }

        @Test
        fun `parses function with expression in tail`() {
            val result = Parser().parseSingleExpression(listOf(
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
            val result = Parser().parseSingleExpression(listOf(
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

            val result = parser.parseSingleExpression(tokens)
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

            val result = parser.parseSingleExpression(tokens)
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
                parser.parseSingleExpression(tokens)
            }
        }

        @Test
        fun `empty let environment throws`() {
            val tokenizer = Tokenizer()
            val parser = Parser()
            val tokens = tokenizer.scan("(let ((a 1)) ())")

            assertThrows(ParsingException::class.java) {
                parser.parseSingleExpression(tokens)
            }
        }
    }

    @Nested
    inner class IfExpression {
        @Test
        fun `allows boolean in pos 1`() {
            val tokenizer = Tokenizer()
            val parser = Parser()
            val tokens = tokenizer.scan("(if true 0 1)")

            val result = parser.parseSingleExpression(tokens)

            assertIsKeyword(KeywordType.IF, result.head)
            assertIsBoolean(true, result.tail[0])
            assertIsNumber(0f, result.tail[1])
            assertIsNumber(1f, result.tail[2])
        }

        @Test
        fun `allows expr in pos 1`() {
            val tokenizer = Tokenizer()
            val parser = Parser()
            val tokens = tokenizer.scan("(if (f 3) 0 1)")

            val result = parser.parseSingleExpression(tokens)

            assertIsKeyword(KeywordType.IF, result.head)
            assertIsExpression({expr ->
                assertIsSymbol("f", expr.head)
                assertIsNumber(3f, expr.tail[0])
            }, result.tail[0])
            assertIsNumber(0f, result.tail[1])
            assertIsNumber(1f, result.tail[2])
        }

        @Test
        fun `allows expressions in pos 2 & pos 3`() {
            val tokenizer = Tokenizer()
            val parser = Parser()
            val tokens = tokenizer.scan("(if true (f 0) (f 1))")

            val result = parser.parseSingleExpression(tokens)

            assertIsKeyword(KeywordType.IF, result.head)
            assertIsBoolean(true, result.tail[0])
            assertIsExpression({expr ->
                assertIsSymbol("f", expr.head)
                assertIsNumber(0f, expr.tail[0])
            }, result.tail[1])
            assertIsExpression({expr ->
                assertIsSymbol("f", expr.head)
                assertIsNumber(1f, expr.tail[0])
            }, result.tail[2])
        }

        @Test
        fun `must have correct number of parts`() {
            assertThrows(ParsingException::class.java) {
                Parser().parseSingleExpression(Tokenizer().scan("(if true 1)"))
            }

            assertThrows(ParsingException::class.java) {
                Parser().parseSingleExpression(Tokenizer().scan("(if true)"))
            }

            assertThrows(ParsingException::class.java) {
                Parser().parseSingleExpression(Tokenizer().scan("(if)"))
            }
        }
    }

    @Nested
    inner class FunExpression {
        @Test
        fun `parses identity function`() {
            val result = getParseTree("(fun id (x) x)")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("id", result.tail[0])
            assertIsExpression({expr ->
                assertIsSymbol("x", expr.head)
            }, result.tail[1])
            assertIsSymbol("x", result.tail[2])
        }

        @Test
        fun `name must be a symbol`() {
            assertThrows(ParsingException::class.java) {
                getParseTree("(fun 1 (x) x)")
            }
            assertThrows(ParsingException::class.java) {
                getParseTree("(fun (thing) (x) x)")
            }
        }

        @Test
        fun `function with strange amounts of parts not allowed`() {
            assertThrows(ParsingException::class.java) { getParseTree("(fun)") }
            assertThrows(ParsingException::class.java) { getParseTree("(fun id (a) 3 3)") }
            assertThrows(ParsingException::class.java) { getParseTree("(fun id (a) 3 3 3)") }
        }

        @Test
        fun `function with no args section allowed`() {
            val result = getParseTree("(fun id 3)")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("id", result.tail[0])
            assertIsNumber(3f, result.tail[1])
        }

        @Test
        fun `function with two arg`() {
            val result = getParseTree("(fun foo (a b) a)")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("foo", result.tail[0])
            assertIsExpression({expr ->
                assertIsSymbol("a", expr.head)
                assertIsSymbol("b", expr.tail[0])
            }, result.tail[1])
            assertIsSymbol("a", result.tail[2])
        }

        @Test
        fun `things which are not symbols not allowed in args list`() {
            assertThrows(ParsingException::class.java) {
                getParseTree("(fun foo (1) x)")
            }
            assertThrows(ParsingException::class.java) {
                getParseTree("(fun foo ((x)) x)")
            }
        }

        @Test
        fun `allows expression return`() {
            val result = getParseTree("(fun foo (g) (g 1))")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("foo", result.tail[0])
            assertIsExpression({expr ->
                assertIsSymbol("g", expr.head)
            }, result.tail[1])
            assertIsExpression({expr ->
                assertIsSymbol("g", expr.head)
                assertIsNumber(1f, expr.tail[0])
            }, result.tail[2])
        }
    }

    @Nested
    inner class ParseMany {
        @Test
        fun `parse single expression`() {
            val tokens = getTokenStream("( f 1 2 )")
            val parser = Parser()
            val result = parser.parse(tokens)
            assertEquals(ExpressionPartType.SYMBOL, result.first().head.type)
            assertEquals(ExpressionPartType.NUMBER, result.first().tail[0].type)
            assertEquals(1f, result.first().tail[0].value)
            assertEquals(ExpressionPartType.NUMBER, result.first().tail[1].type)
            assertEquals(2f, result.first().tail[1].value)
        }

        @Test
        fun `parse multiple expressions`() {
            val tokens = getTokenStream("  ( f 1 2 )        (g 3)")
            val parser = Parser()
            val result = parser.parse(tokens)
            assertEquals(ExpressionPartType.SYMBOL, result.first().head.type)
            assertEquals("f", result.first().head.name)
            assertEquals(ExpressionPartType.NUMBER, result.first().tail[0].type)
            assertEquals(1f, result.first().tail[0].value)
            assertEquals(ExpressionPartType.NUMBER, result.first().tail[1].type)
            assertEquals(2f, result.first().tail[1].value)

            assertEquals(ExpressionPartType.SYMBOL, result[1].head.type)
            assertEquals("g", result[1].head.name)
            assertEquals(ExpressionPartType.NUMBER, result[1].tail[0].type)
            assertEquals(3f, result[1].tail[0].value)
        }
    }

    fun getTokenStream(text: String): List<Token> {
        val tokenizer = Tokenizer()
        return tokenizer.scan(text)
    }

    fun getParseTree(text: String): Expression {
        val tokenizer = Tokenizer()
        val parser = Parser()
        val tokens = tokenizer.scan(text)
        return parser.parseSingleExpression(tokens)
    }

    fun assertIsExpression(expressionAssertion: (Expression) -> Unit, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.EXPRESSION, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNotNull(actual.expression)
        expressionAssertion(actual.expression!!) // Value verified by previous line
        assertNull(actual.keywordType)
        assertNull(actual.innerText)
    }

    fun assertIsNumber(expected: Float, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.NUMBER, actual.type)
        assertNotNull(actual.value)
        assertEquals(expected, actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
        assertNull(actual.keywordType)
        assertNull(actual.innerText)
    }

    fun assertIsBoolean(expected: Boolean, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.BOOLEAN, actual.type)
        assertNull(actual.value)
        assertNotNull(actual.truth)
        assertEquals(expected, actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
        assertNull(actual.keywordType)
        assertNull(actual.innerText)
    }

    fun assertIsSymbol(expected: String, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.SYMBOL, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNotNull(actual.name)
        assertEquals(expected, actual.name)
        assertNull(actual.expression)
        assertNull(actual.keywordType)
        assertNull(actual.innerText)
    }

    fun assertIsKeyword(expected: KeywordType, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.KEYWORD, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
        assertNotNull(actual.keywordType)
        assertEquals(expected, actual.keywordType)
        assertNull(actual.innerText)
    }

    fun assertIsString(expectedText: String, actual: ExpressionPart) {
        assertEquals(ExpressionPartType.STRING, actual.type)
        assertNull(actual.value)
        assertNull(actual.truth)
        assertNull(actual.name)
        assertNull(actual.expression)
        assertNull(actual.keywordType)
        assertNotNull(actual.innerText)
        assertEquals(expectedText, actual.innerText)
    }
}
