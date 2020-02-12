package com.ryanpmartz.lox.tool

import com.ryanpmartz.lox.Expr
import com.ryanpmartz.lox.Stmt

class StatementPrinter {

    fun print(stmt: Stmt?) {

        when (stmt) {
            is Stmt.Expression -> handleExpression(stmt)
        }

    }

    private fun handleExpression(expression: Stmt.Expression) {
        val expr = expression.expression
        when (expr) {
            is Expr.Literal -> println(expr.value)
            is Expr.Binary -> printBinary(expr)
        }

    }

    private fun printBinary(binary: Expr.Binary) {
        println("left: ${binary.left} operator: ${binary.operator} right: ${binary.right}")
    }
}