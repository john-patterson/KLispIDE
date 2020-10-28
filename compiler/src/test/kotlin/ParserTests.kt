import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.exceptions.ParsingException
import com.statelesscoder.klisp.compiler.expressions.Expression
import com.statelesscoder.klisp.compiler.expressions.ExpressionPart
import com.statelesscoder.klisp.compiler.types.*
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
                Parser().parseSingleExpression(listOf(
                    identifierToken(
                        "foo",
                        0
                    ), rightParensToken(3)
                ))
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
                rightParensToken(4)
            ))
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
                rightParensToken(15)
            ))

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
                identifierToken("f", 1),
                leftParensToken(2),
                identifierToken("g", 3),
                numericToken("1", 4),
                rightParensToken(5),
                numericToken("2", 6),
                rightParensToken(7)
            ))

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
                rightParensToken(6)
            ))

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
            val result = getParseTree("(fun id [x] x)")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("id", result.tail[0])
            assertIsList({ls ->
                assertIsSymbol("x", ls.unrealizedItems[0])
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
            assertThrows(ParsingException::class.java) { getParseTree("(fun id [a] 3 3)") }
            assertThrows(ParsingException::class.java) { getParseTree("(fun id [a] 3 3 3)") }
        }

        @Test
        fun `function with no args section allowed`() {
            val result = getParseTree("(fun id 3)")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("id", result.tail[0])
            assertIsList({ assertTrue(it.unrealizedItems.isEmpty()) }, result.tail[1])
            assertIsNumber(3f, result.tail[2])
        }

        @Test
        fun `function with empty args section allowed`() {
            val result = getParseTree("(fun id [] 3)")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("id", result.tail[0])
            assertIsList({ assertEquals(emptyList<ExpressionPart>(), it.unrealizedItems) },
                result.tail[1])
            assertIsNumber(3f, result.tail[2])
        }

        @Test
        fun `function with two arg`() {
            val result = getParseTree("(fun foo [a b] a)")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("foo", result.tail[0])
            assertIsList({list ->
                assertIsSymbol("a", list.unrealizedItems[0])
                assertIsSymbol("b", list.unrealizedItems[1])
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
            val result = getParseTree("(fun foo [g] (g 1))")
            assertIsKeyword(KeywordType.FUN, result.head)
            assertIsSymbol("foo", result.tail[0])
            assertIsList({ls ->
                assertIsSymbol("g", ls.unrealizedItems[0])
            }, result.tail[1])
            assertIsExpression({expr ->
                assertIsSymbol("g", expr.head)
                assertIsNumber(1f, expr.tail[0])
            }, result.tail[2])
        }
    }

    @Nested
    inner class ListParsing {
        @Test
        fun `parse empty list`() {
            val tokens = getTokenStream("(f [])")
            val parser = Parser()
            val result = parser.parse(tokens)
            assertEquals(1, result.size)
            val expr = result.first()
            assertIsSymbol("f", expr.head)
            assertEquals(1, expr.tail.size)
            assertIsList({got ->
                assertEquals(0, got.unrealizedItems.size)
            }, expr.tail.first())
        }

        @Test
        fun `parse single list`() {
            val tokens = getTokenStream("(f [1])")
            val parser = Parser()
            val result = parser.parse(tokens)
            assertEquals(1, result.size)
            val expr = result.first()
            assertIsSymbol("f", expr.head)
            assertEquals(1, expr.tail.size)
            assertIsList({got ->
                assertEquals(1, got.unrealizedItems.size)
                assertIsNumber(1f, got.unrealizedItems.first())
            }, expr.tail.first())
        }

        @Test
        fun `parse list with many things`() {
            val tokens = getTokenStream("(f [1 b])")
            val parser = Parser()
            val result = parser.parse(tokens)
            assertEquals(1, result.size)
            val expr = result.first()
            assertIsSymbol("f", expr.head)
            assertEquals(1, expr.tail.size)
            assertIsList({got ->
                assertEquals(2, got.unrealizedItems.size)
                assertIsNumber(1f, got.unrealizedItems.first())
                assertIsSymbol("b", got.unrealizedItems.last())
            }, expr.tail.first())
        }
    }

    @Nested
    inner class ParseMany {
        @Test
        fun `parse single expression`() {
            val tokens = getTokenStream("( f 1 2 )")
            val parser = Parser()
            val result = parser.parse(tokens)
            assertTrue(result.first().head is Symbol)
            assertTrue(result.first().tail[0] is Data)
            assertEquals(1f, (result.first().tail[0] as Data).numericValue)
            assertEquals(2f, (result.first().tail[1] as Data).numericValue)
        }

        @Test
        fun `parse multiple expressions`() {
            val tokens = getTokenStream("  ( f 1 2 )        (g 3)")
            val parser = Parser()
            val result = parser.parse(tokens)

            assertEquals(Symbol("f"), result.first().head)
            assertEquals(Data(1f), result.first().tail[0])
            assertEquals(Data(2f), result.first().tail[1])

            assertEquals(Symbol("g"), result[1].head)
            assertEquals(Data(3f), result[1].tail[0])
        }

        @Test
        fun `parse multiple nested expressions`() {
            val tokens = getTokenStream("(fun foo [a b] (+ a b 1))(foo 10 20)")
            val parser = Parser()
            val result = parser.parse(tokens)
            assertEquals(Keyword(KeywordType.FUN), result.first().head)
            assertEquals(Symbol("foo"), result.first().tail[0])

            val paramList = result.first().tail[1] as KList
            assertEquals(Symbol("a"), paramList.unrealizedItems[0])
            assertEquals(Symbol("b"), paramList.unrealizedItems[1])

            val functionBody = result.first().tail[2] as Expression
            assertEquals(Symbol("+"), functionBody.head)
            assertEquals(Symbol("a"), functionBody.tail[0])
            assertEquals(Symbol("b"), functionBody.tail[1])
            assertEquals(Data(1f), functionBody.tail[2])

            assertEquals(Symbol("foo"), result[1].head)
            assertEquals(Data(10f), result[1].tail[0])
            assertEquals(Data(20f), result[1].tail[1])
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
        assertTrue(actual is Expression)
        expressionAssertion(actual as Expression)
    }

    fun assertIsNumber(expected: Float, actual: ExpressionPart) {
        assertTrue(actual is Data)
        assertEquals(expected, (actual as Data).numericValue)
    }

    fun assertIsBoolean(expected: Boolean, actual: ExpressionPart) {
        assertTrue(actual is Data)
        assertEquals(expected, (actual as Data).truthyValue)
    }

    fun assertIsSymbol(expected: String, actual: ExpressionPart) {
        assertTrue(actual is Symbol)
        assertEquals(expected, (actual as Symbol).symbolName)
    }

    fun assertIsKeyword(expected: KeywordType, actual: ExpressionPart) {
        assertTrue(actual is Keyword)
        assertEquals(expected, (actual as Keyword).kwdType)
    }

    fun assertIsString(expectedText: String, actual: ExpressionPart) {
        assertTrue(actual is Data)
        assertEquals(expectedText, (actual as Data).stringValue)
    }

    fun assertIsList(listAssertion: (KList) -> Unit, actual: ExpressionPart) {
        assert(actual is KList)
        listAssertion(actual as KList)
    }
}
