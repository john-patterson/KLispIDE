import com.statelesscoder.klisp.compiler.*
import com.statelesscoder.klisp.compiler.Function
import com.statelesscoder.klisp.compiler.UserDefinedFunction
import com.statelesscoder.klisp.compiler.exceptions.ScopeDataException
import com.statelesscoder.klisp.compiler.types.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTests {
    @Test
    fun `built-in math functions`() {
        assertEquals(3f, run<KLNumber>("(+ 1 2)").value)
        assertEquals(1f, run<KLNumber>("(- 2 1)").value)
        assertEquals(20f, run<KLNumber>("(* 2 10)").value)
        assertEquals(12.5f, run<KLNumber>("(/ 25 2)").value)
        assertEquals(4f, run<KLNumber>("(/ (* (- 3 1) 10) 5)").value)
    }

    @Test
    fun `built-in print function`() {
        assertEquals("hello world", (run<KLString>("(print \"hello\" \"world\")")).text)
    }

    @Test
    fun `let bindings, if expressions, and function declaration`() {
        val scope = Scope()
        run<KLValue>("(fun! foo [a b c] (if a b c))", scope)
        assertNotNull(scope.lookup(Symbol("foo")))

        val executionResult = run<KLNumber>("(let ((switch false)) (foo switch 50 100))", scope)
        assertEquals(100f, executionResult.value)
    }

    @Test
    fun `cons builds up a list`() {
        val result = run<RealizedList>("(cons (cons (cons [] 1f) true) \"okay\")")
        assertEquals(1f, (result.items[0] as KLNumber).value)
        assertEquals(true, (result.items[1] as KLBool).truth)
        assertEquals("okay", (result.items[2] as KLString).text)
        assertEquals("[1.0 true \"okay\"]", result.toString())
    }

    @Test
    fun `can get third item of a list`() {
        val result = run<KLNumber>("(car (cdr (cdr [1f 2f 3f 4f])))")
        assertEquals(3f, result.value)
    }

    @Test
    fun `function equality`() {
        val scope = Scope()
        run<UserDefinedFunction>("(fun! f [a b] (and a b))", scope)
        run<UserDefinedFunction>("(fun! g [a b] (and a b))", scope)
        val resultSameName = run<KLBool>("(eq f f)", scope)
        val resultOtherName = run<KLBool>("(eq f g)", scope)
        assertEquals(true, resultSameName.truth)
        assertEquals(false, resultOtherName.truth)
    }

    @Test
    fun `symbolic equality`() {
        val resultSame = run<KLBool>("(let ((a 1) (b 1)) (eq a b))")
        val resultDifferent = run<KLBool>("(let ((a 1) (b 4)) (eq a b))")
        assertEquals(true, resultSame.truth)
        assertEquals(false, resultDifferent.truth)
    }

    @Test
    fun `recursive function`() {
        val scope = Scope()
        run<UserDefinedFunction>("(fun! f [n] (if (eq n 0) 0 (+ n (f (- n 1)))))", scope)
        val executionResult = run<KLNumber>("(f 3)", scope)
        assertEquals(6f, executionResult.value)
    }

    @Test
    fun `filter definable`() {
        val scope = Scope()
        run<UserDefinedFunction>("(fun! filter [ls nls f] " +
                "(if (eq ls []) " +
                    "nls " +
                    "(if (f (car ls)) " +
                        "(filter (cdr ls) (cons nls (car ls)) f) " +
                        "(filter (cdr ls) nls f))))", scope)
        val executionResult = run("(filter [1 2 1 3] [] (fun! foo [a] (eq 1 a)))", scope) as RealizedList
        assertEquals(2, executionResult.items.size)
    }

    @Test
    fun `built-ins are first-class citizens`() {
        val scope = Scope()
        run<Function>("(fun! foo [f] (f 2 5))", scope)
        val result = run<KLNumber>("(foo +)", scope)
        assertEquals(7f, result.value)
    }

    @Test
    fun `constant functions`() {
        val scope = Scope()
        run<KLNumber>("(fun! f1 2)", scope)
        run<KLNumber>("(fun! f2 [] 3)", scope)
        val result = run<KLNumber>("(+ (f1) (f2))", scope)
        assertEquals(5f, result.value)
    }

    @Test
    fun `anonymous functions definable`() {
        val fnVal = run<Function>("(fun [x] 2)")
        assertNotNull(fnVal)
    }

    @Test
    fun `anonymous functions without arguments definable`() {
        val fnEmptyParams = run<Function>("(fun [] 2)")
        assertNotNull(fnEmptyParams)

        val fnNoParams = run<Function>("(fun 2)")
        assertNotNull(fnNoParams)
    }

    @Test
    fun `anonymous functions runnable`() {
        val result = run<KLNumber>("((fun [x] (+ x 1)) 5)")
        assertEquals(6f, result.value)
    }

    @Test
    fun `inner scope function definition does not update the parent scope`() {
        val scope = Scope()
        val result = run<KLNumber>("(let ((a 1)) ((fun! f [x] (+ 1 x)) a))", scope)
        assertEquals(2f, result.value)
        assertThrows(ScopeDataException::class.java) {
            scope.lookup(Symbol("f"))
        }
    }

    @Test
    fun `function definition has closure`() {
        val result = run<KLNumber>("((let ((a 1)) (fun! f [x] (+ x a))) 1)")
        assertEquals(2f, result.value)
    }

    private fun <T : KLValue> run(text: String, env: Scope = Scope()): T {
        val tokens = Tokenizer().scan(text)
        val ast = Parser().parseSingleExpression(tokens)
        return Executor().execute(ast, env) as T
    }
}