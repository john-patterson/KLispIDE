import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.types.Data
import com.statelesscoder.klisp.compiler.types.DataType
import com.statelesscoder.klisp.compiler.types.KLString
import com.statelesscoder.klisp.compiler.types.RealizedList
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
        assertEquals("hello world", (run("(print \"hello\" \"world\")").literal as KLString).text)
    }

    @Test
    fun `let bindings, if expressions, and function declaration`() {
        val scope = Scope()
        val bindingResult = run("(fun foo [a b c] (if a b c))", scope)
        assertEquals(DataType.FUNCTION, bindingResult.dataType)
        assertNotNull(scope.lookup(Symbol("foo")))

        val executionResult = run("(let ((switch false)) (foo switch 50 100))", scope)
        assertEquals(100f, executionResult.numericValue)
    }

    @Test
    fun `cons builds up a list`() {
        val result = run("(cons (cons (cons [] 1f) true) \"okay\")") as RealizedList
        assertEquals(1f, result.items[0].numericValue)
        assertEquals(true, result.items[1].truthyValue)
        assertEquals("okay", (result.items[2] as KLString).text)
        assertEquals("[1.0 true \"okay\"]", result.toString())
    }

    @Test
    fun `can get third item of a list`() {
        val result = run("(car (cdr (cdr [1f 2f 3f 4f])))")
        assertEquals(DataType.NUMBER, result.dataType)
        assertEquals(3f, result.numericValue)
    }

    @Test
    fun `function equality`() {
        val scope = Scope()
        run("(fun f [a b] (and a b))", scope)
        run("(fun g [a b] (and a b))", scope)
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

    @Test
    fun `recursive function`() {
        val scope = Scope()
        val bindingResult = run("(fun f [n] (if (eq n 0) 0 (+ n (f (- n 1)))))", scope)
        assertEquals(DataType.FUNCTION, bindingResult.dataType)
        val executionResult = run("(f 3)", scope)
        assertEquals(6f, executionResult.numericValue)
    }

    @Test
    fun `filter definable`() {
        val scope = Scope()
        val bindingResult = run("(fun filter [ls nls f] " +
                "(if (eq ls []) " +
                    "nls " +
                    "(if (f (car ls)) " +
                        "(filter (cdr ls) (cons nls (car ls)) f) " +
                        "(filter (cdr ls) nls f))))", scope)
        assertEquals(DataType.FUNCTION, bindingResult.dataType)
        val executionResult = run("(filter [1 2 1 3] [] (fun foo [a] (eq 1 a)))", scope) as RealizedList
        assertEquals(2, executionResult.items.size)
    }

    @Test
    fun `constant functions`() {
        val scope = Scope()
        run("(fun f1 2)", scope)
        run("(fun f2 [] 3)", scope)
        val result = run("(+ (f1) (f2))", scope)
        assertEquals(5f, result.numericValue)
    }

    private fun run(text: String, env: Scope = Scope()): Data {
        val tokens = Tokenizer().scan(text)
        val ast = Parser().parseSingleExpression(tokens)
        return Executor().execute(ast, env)
    }
}