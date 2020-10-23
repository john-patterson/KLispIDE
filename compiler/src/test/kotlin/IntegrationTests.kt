import com.statelesscoder.klisp.compiler.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTests {
    @Test
    fun `built-in math functions`() {
        assertEquals(3f, run("(+ 1 2)").numericValue)
        assertEquals(1f, run("(- 2 1)").numericValue)
        assertEquals(20f, run("(* 2 10)").numericValue)
        assertEquals(12.5f, run("(/ 25 2)").numericValue)
        assertEquals(4f, run("(/ (* (- 3 1) 10) 5)").numericValue)
    }

    @Test
    fun `built-in print function`() {
        assertEquals("hello world", run("(print \"hello\" \"world\")").stringValue)
    }

    @Test
    fun `let bindings, if expressions, and function declaration`() {
        val scope = Scope()
        val bindingResult = run("(fun foo (a b c) (if a b c))", scope)
        assertEquals(DataType.FUNCTION, bindingResult.type)
        assertNotNull(scope.lookup("foo"))

        val executionResult = run("(let ((switch false)) (foo switch 50 100))", scope)
        assertEquals(100f, executionResult.numericValue)
    }

    private fun run(text: String, env: Scope = Scope()): Data {
        val tokens = Tokenizer().scan(text)
        val ast = Parser().parseSingleExpression(tokens)
        return Executor().execute(ast, env)
    }
}