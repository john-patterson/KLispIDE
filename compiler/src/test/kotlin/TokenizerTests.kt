import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.exceptions.ScanningException
import com.statelesscoder.klisp.compiler.types.Token
import com.statelesscoder.klisp.compiler.types.TokenType
import com.statelesscoder.klisp.compiler.types.stringToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenizerTests {
    @Nested
    inner class ListTests {
        @Test
        fun `empty expr`() {
            val result = Tokenizer().scan("[]")
            val (leftBracket, rightBracket) = result
            assertEquals(2, result.size)
            assertLeftBracket(0, leftBracket)
            assertRightBracket(1, rightBracket)
        }

        @Test
        fun `skips whitespace between components`() {
            val result = Tokenizer().scan("      [             ]     ")
            val (leftBracket, rightBracket) = result
            assertEquals(2, result.size)
            assertLeftBracket(6, leftBracket)
            assertRightBracket(20, rightBracket)
        }

        @Test
        fun `tokenizes list with mixed types`() {
            val result = Tokenizer().scan("[g 1]")
            assertEquals(4, result.size)
            assertLeftBracket(0, result[0])
            assertEquals(
                Token(
                    "g",
                    TokenType.IDENTIFIER,
                    1
                ), result[1])
            assertEquals(
                Token(
                    "1",
                    TokenType.NUMERIC,
                    3
                ), result[2])
            assertRightBracket(4, result[3])
        }

        private fun assertLeftBracket(pos: Int, leftBracket: Token) {
            assertEquals(
                Token(
                    "[",
                    TokenType.LEFT_BRACKET,
                    pos
                ), leftBracket)
        }

        private fun assertRightBracket(pos: Int, rightBracket: Token) {
            assertEquals(
                Token(
                    "]",
                    TokenType.RIGHT_BRACKET,
                    pos
                ), rightBracket)
        }
    }

    @Nested
    inner class ExpressionTests {
        @Test
        fun `empty expr`() {
            val result = Tokenizer().scan("()")
            val (leftParen, rightParen) = result
            assertEquals(2, result.size)
            assertLeftParen(0, leftParen)
            assertRightParen(1, rightParen)
        }

        @Test
        fun `skips whitespace between components`() {
            val result = Tokenizer().scan("      (             )     ")
            val (leftParen, rightParen) = result
            assertEquals(2, result.size)
            assertLeftParen(6, leftParen)
            assertRightParen(20, rightParen)
        }

        @Test
        fun `tokenizes function invocation with args`() {
            val result = Tokenizer().scan("(g 1)")
            assertEquals(4, result.size)
            assertLeftParen(0, result[0])
            assertEquals(
                Token(
                    "g",
                    TokenType.IDENTIFIER,
                    1
                ), result[1])
            assertEquals(
                Token(
                    "1",
                    TokenType.NUMERIC,
                    3
                ), result[2])
            assertRightParen(4, result[3])
        }

        private fun assertLeftParen(pos: Int, leftParen: Token) {
            assertEquals(
                Token(
                    "(",
                    TokenType.LEFT_PARENS,
                    pos
                ), leftParen)
        }

        private fun assertRightParen(pos: Int, rightParen: Token) {
            assertEquals(
                Token(
                    ")",
                    TokenType.RIGHT_PARENS,
                    pos
                ), rightParen)
        }
    }

    @Nested
    inner class ReservedKeywordsTests {
        @Test
        fun `classifies if`() {
            val result = Tokenizer().scan("if IF")
            assertEquals(2, result.size)
            assertEquals(
                Token(
                    "if",
                    TokenType.IF,
                    0
                ), result[0])
            assertEquals(
                Token(
                    "IF",
                    TokenType.IF,
                    3
                ), result[1])
        }

        @Test
        fun `classifies let`() {
            val result = Tokenizer().scan("let LET")
            assertEquals(2, result.size)
            assertEquals(
                Token(
                    "let",
                    TokenType.LET,
                    0
                ), result[0])
            assertEquals(
                Token(
                    "LET",
                    TokenType.LET,
                    4
                ), result[1])
        }

        @Test
        fun `classifies fun`() {
            val result = Tokenizer().scan("fun fUn")
            assertEquals(2, result.size)
            assertEquals(
                Token(
                    "fun",
                    TokenType.FUN,
                    0
                ), result[0])
            assertEquals(
                Token(
                    "fUn",
                    TokenType.FUN,
                    4
                ), result[1])
        }

        @Test
        fun `classifies true`() {
            val result = Tokenizer().scan("true tRUe")
            assertEquals(2, result.size)
            assertEquals(
                Token(
                    "true",
                    TokenType.BOOLEAN,
                    0
                ), result[0])
            assertEquals(
                Token(
                    "tRUe",
                    TokenType.BOOLEAN,
                    5
                ), result[1])
        }

        @Test
        fun `classifies false`() {
            val result = Tokenizer().scan("false FalsE")
            assertEquals(2, result.size)
            assertEquals(
                Token(
                    "false",
                    TokenType.BOOLEAN,
                    0
                ), result[0])
            assertEquals(
                Token(
                    "FalsE",
                    TokenType.BOOLEAN,
                    6
                ), result[1])
        }
    }

    @Nested
    inner class SymbolClassificationTests {
        @Test
        fun `classifies symbols`() {
            val result = Tokenizer().scan("abc abc123 123ABc .... ___%&")
            assertEquals(5, result.size)
            assertEquals(
                Token(
                    "abc",
                    TokenType.IDENTIFIER,
                    0
                ), result[0])
            assertEquals(
                Token(
                    "abc123",
                    TokenType.IDENTIFIER,
                    4
                ), result[1])
            assertEquals(
                Token(
                    "123ABc",
                    TokenType.IDENTIFIER,
                    11
                ), result[2])
            assertEquals(
                Token(
                    "....",
                    TokenType.IDENTIFIER,
                    18
                ), result[3])
            assertEquals(
                Token(
                    "___%&",
                    TokenType.IDENTIFIER,
                    23
                ), result[4])
        }
    }

    @Nested
    inner class NumberClassificationTests {
        @Test
        fun `classifies positive integer`() {
            val result = Tokenizer().scan("123")
            assertEquals(1, result.size)
            assertEquals(
                Token(
                    "123",
                    TokenType.NUMERIC,
                    0
                ), result[0])
        }

        @Test
        fun `classifies negative integer`() {
            val result = Tokenizer().scan("-123")
            assertEquals(1, result.size)
            assertEquals(
                Token(
                    "-123",
                    TokenType.NUMERIC,
                    0
                ), result[0])
        }

        @Test
        fun `classifies zero`() {
            val result = Tokenizer().scan("0")
            assertEquals(1, result.size)
            assertEquals(
                Token(
                    "0",
                    TokenType.NUMERIC,
                    0
                ), result[0])
        }

        @Test
        fun `classifies positive floating number`() {
            val result = Tokenizer().scan("1.23")
            assertEquals(1, result.size)
            assertEquals(
                Token(
                    "1.23",
                    TokenType.NUMERIC,
                    0
                ), result[0])
        }

        @Test
        fun `classifies positive floating negative`() {
            val result = Tokenizer().scan("-1.23")
            assertEquals(1, result.size)
            assertEquals(
                Token(
                    "-1.23",
                    TokenType.NUMERIC,
                    0
                ), result[0])
        }

        @Test
        fun `classifies positive floating zero`() {
            val result = Tokenizer().scan("0.00")
            assertEquals(1, result.size)
            assertEquals(
                Token(
                    "0.00",
                    TokenType.NUMERIC,
                    0
                ), result[0])
        }

        @Test
        fun `classifies floating number with no leading number`() {
            val result = Tokenizer().scan(".123")
            assertEquals(1, result.size)
            assertEquals(
                Token(
                    ".123",
                    TokenType.NUMERIC,
                    0
                ), result[0])
        }
    }

    @Nested
    inner class StringClassificationTests {
        @Test
        fun `scans empty string`() {
            val result = Tokenizer().scan("\"\"")
            assertEquals(1, result.size)
            assertEquals(stringToken("\"\"", 0), result[0])
        }

        @Test
        fun `scans string with one thing`() {
            val result = Tokenizer().scan("\"a\"")
            assertEquals(1, result.size)
            assertEquals(stringToken("\"a\"", 0), result[0])
        }

        @Test
        fun `scans string with multiple things`() {
            val result = Tokenizer().scan("\"ab\"")
            assertEquals(1, result.size)
            assertEquals(stringToken("\"ab\"", 0), result[0])
        }

        @Test
        fun `throws when string is not ended`() {
            assertThrows(ScanningException::class.java) {
                Tokenizer().scan("\"ab")
            }
            assertThrows(ScanningException::class.java) {
                Tokenizer().scan("ab\"")
            }
        }
    }
}