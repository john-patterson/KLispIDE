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
    }

    @Nested
    inner class SymbolClassificationTests {
        @Test
        fun `classifies reserved keywords`() {
            val result = Tokenizer().scan("if IF let LET")
            Assertions.assertEquals(4, result.size)
            Assertions.assertEquals(Token("if", TokenType.IF), result[0])
            Assertions.assertEquals(Token("IF", TokenType.IF), result[1])
            Assertions.assertEquals(Token("let", TokenType.LET), result[2])
            Assertions.assertEquals(Token("LET", TokenType.LET), result[3])
        }

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
        fun `classifies number`() {
            val result = Tokenizer().scan("123 0")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("123", TokenType.NUMERIC), result[0])
            Assertions.assertEquals(Token("0", TokenType.NUMERIC), result[1])
        }

        @Test
        fun `classifies negative and decimals as symbol`() { // This is not the *intentional* spec long-term, but it sure is easier to implement
            val result = Tokenizer().scan("-123 1.23")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("-123", TokenType.NUMERIC), result[0])
            Assertions.assertEquals(Token("1.23", TokenType.IDENTIFIER), result[1])
        }
    }

}