package com.ryanpmartz.lox

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class InterpreterTest {

    private val interpreter = Interpreter()

    @Test
    fun testLiteralExpression() {
        val source = "2;"

        val statements = scan(source)

        val literal = Expr.Literal("2")
        val result = interpreter.visitLiteralExpr(literal)

        assertEquals(result, "2")
    }

    private fun scan(source: String): List<Stmt> {
        val scanner = Scanner(source)
        return Parser(scanner.scanTokens()).parse()
    }


}