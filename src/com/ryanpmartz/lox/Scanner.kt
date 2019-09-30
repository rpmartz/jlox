package com.ryanpmartz.lox

import com.ryanpmartz.lox.TokenType.COMMA
import com.ryanpmartz.lox.TokenType.DOT
import com.ryanpmartz.lox.TokenType.EOF
import com.ryanpmartz.lox.TokenType.LEFT_BRACE
import com.ryanpmartz.lox.TokenType.LEFT_PAREN
import com.ryanpmartz.lox.TokenType.MINUS
import com.ryanpmartz.lox.TokenType.PLUS
import com.ryanpmartz.lox.TokenType.RIGHT_BRACE
import com.ryanpmartz.lox.TokenType.RIGHT_PAREN
import com.ryanpmartz.lox.TokenType.SEMICOLON
import com.ryanpmartz.lox.TokenType.STAR

import java.util.ArrayList

class Scanner(private val source: String) {
    private val tokens = ArrayList<Token>()

    private var start = 0
    private var current = 0
    private val line = 1

    private val isAtEnd: Boolean
        get() = current >= source.length

    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    // each turn of the loop, scan a single token
    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            else -> Lox.error(line, "Unexpected character")
        }
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}
