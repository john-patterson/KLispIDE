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
            Assertions.assertEquals(Token("(", TokenType.LEFT_PARENS), leftParen)
            Assertions.assertEquals(Token(")", TokenType.RIGHT_PARENS), rightParen)
        }

        @Test
        fun `skips whitespace between components`() {
            val result = Tokenizer().scan("      (             )     ")
            val (leftParen, rightParen) = result
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("(", TokenType.LEFT_PARENS), leftParen)
            Assertions.assertEquals(Token(")", TokenType.RIGHT_PARENS), rightParen)
        }
    }

    @Nested
    inner class ReservedKeywordsTests {
        @Test
        fun `classifies if`() {
            val result = Tokenizer().scan("if IF")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("if", TokenType.IF), result[0])
            Assertions.assertEquals(Token("IF", TokenType.IF), result[1])
        }

        @Test
        fun `classifies let`() {
            val result = Tokenizer().scan("let LET")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("let", TokenType.LET), result[0])
            Assertions.assertEquals(Token("LET", TokenType.LET), result[1])
        }

        @Test
        fun `classifies fun`() {
            val result = Tokenizer().scan("fun fUn")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("fun", TokenType.FUN), result[0])
            Assertions.assertEquals(Token("fUn", TokenType.FUN), result[1])
        }

        @Test
        fun `classifies true`() {
            val result = Tokenizer().scan("true tRUe")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("true", TokenType.BOOLEAN), result[0])
            Assertions.assertEquals(Token("tRUe", TokenType.BOOLEAN), result[1])
        }

        @Test
        fun `classifies false`() {
            val result = Tokenizer().scan("false FalsE")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("false", TokenType.BOOLEAN), result[0])
            Assertions.assertEquals(Token("FalsE", TokenType.BOOLEAN), result[1])
        }
    }

    @Nested
    inner class SymbolClassificationTests {
        @Test
        fun `classifies symbols`() {
            val result = Tokenizer().scan("abc abc123 123ABc .... ___%&")
            Assertions.assertEquals(5, result.size)
            Assertions.assertEquals(Token("abc", TokenType.IDENTIFIER), result[0])
            Assertions.assertEquals(Token("abc123", TokenType.IDENTIFIER), result[1])
            Assertions.assertEquals(Token("123ABc", TokenType.IDENTIFIER), result[2])
            Assertions.assertEquals(Token("....", TokenType.IDENTIFIER), result[3])
            Assertions.assertEquals(Token("___%&", TokenType.IDENTIFIER), result[4])
        }
    }

    @Nested
    inner class NumberClassificationTests {
        @Test
        fun `classifies positive integer`() {
            val result = Tokenizer().scan("123")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("123", TokenType.NUMERIC), result[0])
        }

        @Test
        fun `classifies negative integer`() {
            val result = Tokenizer().scan("-123")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("-123", TokenType.NUMERIC), result[0])
        }

        @Test
        fun `classifies zero`() {
            val result = Tokenizer().scan("0")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("0", TokenType.NUMERIC), result[0])
        }

        @Test
        fun `classifies positive floating number`() {
            val result = Tokenizer().scan("1.23")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("1.23", TokenType.NUMERIC), result[0])
        }

        @Test
        fun `classifies positive floating negative`() {
            val result = Tokenizer().scan("-1.23")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("-1.23", TokenType.NUMERIC), result[0])
        }

        @Test
        fun `classifies positive floating zero`() {
            val result = Tokenizer().scan("0.00")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token("0.00", TokenType.NUMERIC), result[0])
        }

        @Test
        fun `classifies floating number with no leading number`() {
            val result = Tokenizer().scan(".123")
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(Token(".123", TokenType.NUMERIC), result[0])
        }
    }

}