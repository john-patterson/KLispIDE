import com.statelesscoder.klisp.compiler.DataType
import com.statelesscoder.klisp.compiler.Executor
import com.statelesscoder.klisp.compiler.ExpressionPart
import com.statelesscoder.klisp.compiler.ExpressionPartType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExecutorTests {
    @Nested
    inner class ExecutePartTests {
        @Test
        fun `handles numbers`() {
            val e = Executor()
            val exprPart = ExpressionPart(ExpressionPartType.NUMBER)
            exprPart.value = 1f
            val result = e.realizePart(exprPart)
            assertEquals(DataType.NUMBER, result.type)
            assertEquals(1f, result.innerValue)
        }

        @Test
        fun `handles strings`() {
            val e = Executor()
            val exprPart = ExpressionPart(ExpressionPartType.STRING)
            exprPart.innerText = "foo test"
            val result = e.realizePart(exprPart)
            assertEquals(DataType.STRING, result.type)
            assertEquals("foo test", result.innerText)
        }

        @Test
        fun `handles booleans`() {
            val e = Executor()
            val exprPart = ExpressionPart(ExpressionPartType.BOOLEAN)
            exprPart.truth = true
            val result = e.realizePart(exprPart)
            assertEquals(DataType.BOOLEAN, result.type)
            assertTrue(result.successful)
        }
    }
}
