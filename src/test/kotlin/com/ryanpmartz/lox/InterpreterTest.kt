package com.ryanpmartz.lox

import org.junit.jupiter.api.Test


class InterpreterTest {

    private val interpreter = Interpreter()

    @Test
    fun testLiteralExpression() {
        val source = "2;"

        val statements = scan(source)
        interpreter.interpret(statements)
    }

    private fun scan(source: String): List<Stmt> {
        val scanner = Scanner(source)
        return Parser(scanner.scanTokens()).parse()
    }


}