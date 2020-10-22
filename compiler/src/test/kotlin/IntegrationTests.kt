import com.statelesscoder.klisp.compiler.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTests {
    @Test
    fun `built-in math functions`() {
        assertEquals(3f, run("(+ 1 2)").innerValue)
        assertEquals(1f, run("(- 2 1)").innerValue)
        assertEquals(20f, run("(* 2 10)").innerValue)
        assertEquals(12.5f, run("(/ 25 2)").innerValue)
        assertEquals(4f, run("(/ (* (- 3 1) 10) 5)").innerValue)
    }

    @Test
    fun `built-in print function`() {
        assertEquals("hello world", run("(print \"hello\" \"world\")").innerText)
    }

    private fun run(text: String, env: Scope = Scope()): ExecutionResult {
        val tokens = Tokenizer().scan(text)
        val ast = Parser().parse(tokens)
        return Executor().execute(ast, env)
    }
}