import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.types.Data
import com.statelesscoder.klisp.compiler.types.DataType
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

    @Test
    fun `cons builds up a list`() {
        val result = run("(cons (cons (cons [] 1f) true) \"okay\")")
        assertEquals(DataType.LIST, result.type)
        assertEquals(1f, result.listValue!!.realizedData[0].numericValue)
        assertEquals(true, result.listValue!!.realizedData[1].truthyValue)
        assertEquals("okay", result.listValue!!.realizedData[2].stringValue)
        assertEquals("[1.0 true \"okay\"]", result.listValue.toString())
    }

    @Test
    fun `can get third item of a list`() {
        val result = run("(car (cdr (cdr [1f 2f 3f 4f])))")
        assertEquals(DataType.NUMBER, result.type)
        assertEquals(3f, result.numericValue)
    }

    @Test
    fun `function equality`() {
        val scope = Scope()
        run("(fun f (a b) (and a b))", scope)
        run("(fun g (a b) (and a b))", scope)
        val resultSameName = run("(eq f f)", scope)
        val resultOtherName = run("(eq f g)", scope)
        assertEquals(true, resultSameName.truthyValue)
        assertEquals(false, resultOtherName.truthyValue)
    }

    @Test
    fun `symbolic equality`() {
        val resultSame = run("(let ((a 1) (b 1)) (eq a b))")
        val resultDifferent = run("(let ((a 1) (b 4)) (eq a b))")
        assertEquals(true, resultSame.truthyValue)
        assertEquals(false, resultDifferent.truthyValue)
    }

    private fun run(text: String, env: Scope = Scope()): Data {
        val tokens = Tokenizer().scan(text)
        val ast = Parser().parseSingleExpression(tokens)
        return Executor().execute(ast, env)
    }
}