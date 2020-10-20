import com.statelesscoder.klisp.compiler.TokenType
import com.statelesscoder.klisp.compiler.Tokenizer
import com.statelesscoder.klisp.compiler.Token
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenizerTests {
    @Nested
    inner class ListTests {
        @Test
        fun `empty list`() {
            val result = Tokenizer().scan("()")
            val (leftParen, rightParen) = result
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("(", TokenType.LEFT_PARENS, 0), leftParen)
            Assertions.assertEquals(Token(")", TokenType.RIGHT_PARENS, 1), rightParen)
        }

        @Test
        fun `skips whitespace between components`() {
            val result = Tokenizer().scan("      (             )     ")
            val (leftParen, rightParen) = result
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("(", TokenType.LEFT_PARENS, 6), leftParen)
            Assertions.assertEquals(Token(")", TokenType.RIGHT_PARENS, 20), rightParen)
        }
    }

    @Nested
    inner class ReservedKeywordsTests {
        @Test
        fun `classifies if`() {
            val result = Tokenizer().scan("if IF")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("if", TokenType.IF, 0), result[0])
            Assertions.assertEquals(Token("IF", TokenType.IF, 3), result[1])
        }

        @Test
        fun `classifies let`() {
            val result = Tokenizer().scan("let LET")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("let", TokenType.LET, 0), result[0])
            Assertions.assertEquals(Token("LET", TokenType.LET, 4), result[1])
        }

        @Test
        fun `classifies fun`() {
            val result = Tokenizer().scan("fun fUn")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("fun", TokenType.FUN, 0), result[0])
            Assertions.assertEquals(Token("fUn", TokenType.FUN, 4), result[1])
        }

        @Test
        fun `classifies true`() {
            val result = Tokenizer().scan("true tRUe")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("true", TokenType.BOOLEAN, 0), result[0])
            Assertions.assertEquals(Token("tRUe", TokenType.BOOLEAN, 5), result[1])
        }

        @Test
        fun `classifies false`() {
            val result = Tokenizer().scan("false FalsE")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("false", TokenType.BOOLEAN, 0), result[0])
            Assertions.assertEquals(Token("FalsE", TokenType.BOOLEAN, 6), result[1])
        }
    }

    @Nested
    inner class SymbolClassificationTests {
        @Test
        fun `classifies symbols`() {
            val result = Tokenizer().scan("abc abc123 123ABc .... ___%&")
            Assertions.assertEquals(5, result.size)
            Assertions.assertEquals(Token("abc", TokenType.IDENTIFIER, 0), result[0])
            Assertions.assertEquals(Token("abc123", TokenType.IDENTIFIER, 4), result[1])
            Assertions.assertEquals(Token("123ABc", TokenType.IDENTIFIER, 11), result[2])
            Assertions.assertEquals(Token("....", TokenType.IDENTIFIER, 18), result[3])
            Assertions.assertEquals(Token("___%&", TokenType.IDENTIFIER, 23), result[4])
        }
    }

    @Nested
    inner class NumberClassificationTests {
        @Test
        fun `classifies positive integer`() {
            val result = Tokenizer().scan("123")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("123", TokenType.NUMERIC, 0), result[0])
        }

        @Test
        fun `classifies negative integer`() {
            val result = Tokenizer().scan("-123")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("-123", TokenType.NUMERIC, 0), result[0])
        }

        @Test
        fun `classifies zero`() {
            val result = Tokenizer().scan("0")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("0", TokenType.NUMERIC, 0), result[0])
        }

        @Test
        fun `classifies positive floating number`() {
            val result = Tokenizer().scan("1.23")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("1.23", TokenType.NUMERIC, 0), result[0])
        }

        @Test
        fun `classifies positive floating negative`() {
            val result = Tokenizer().scan("-1.23")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("-1.23", TokenType.NUMERIC, 0), result[0])
        }

        @Test
        fun `classifies positive floating zero`() {
            val result = Tokenizer().scan("0.00")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("0.00", TokenType.NUMERIC, 0), result[0])
        }

        @Test
        fun `classifies floating number with no leading number`() {
            val result = Tokenizer().scan(".123")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token(".123", TokenType.NUMERIC, 0), result[0])
        }
    }

}