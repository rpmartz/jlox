package com.ryanpmartz.lox;

import static com.ryanpmartz.lox.TokenType.BANG;
import static com.ryanpmartz.lox.TokenType.BANG_EQUAL;
import static com.ryanpmartz.lox.TokenType.EOF;
import static com.ryanpmartz.lox.TokenType.EQUAL_EQUAL;
import static com.ryanpmartz.lox.TokenType.FALSE;
import static com.ryanpmartz.lox.TokenType.GREATER;
import static com.ryanpmartz.lox.TokenType.GREATER_EQUAL;
import static com.ryanpmartz.lox.TokenType.LEFT_PAREN;
import static com.ryanpmartz.lox.TokenType.LESS;
import static com.ryanpmartz.lox.TokenType.LESS_EQUAL;
import static com.ryanpmartz.lox.TokenType.MINUS;
import static com.ryanpmartz.lox.TokenType.NUMBER;
import static com.ryanpmartz.lox.TokenType.PLUS;
import static com.ryanpmartz.lox.TokenType.PRINT;
import static com.ryanpmartz.lox.TokenType.RIGHT_PAREN;
import static com.ryanpmartz.lox.TokenType.SEMICOLON;
import static com.ryanpmartz.lox.TokenType.SLASH;
import static com.ryanpmartz.lox.TokenType.STAR;
import static com.ryanpmartz.lox.TokenType.STRING;
import static com.ryanpmartz.lox.TokenType.TRUE;

import java.util.ArrayList;
import java.util.List;

public class Parser {

	private static class ParseError extends RuntimeException {
	}

	private final List<Token> tokens;
	private int current = 0;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	public List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while (!isAtEnd()) {
			statements.add(statement());
		}

		return statements;
	}

	// when the body of the rule contains a nonterminal - a referrence to another rule, we call that rule's method
	private Expr expression() {
		return equality();
	}

	private Stmt statement() {
		if (match(PRINT)) {
			return printStatement();
		}

		return expressionStatement();
	}

	private Stmt printStatement() {
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value");
		return new Stmt.Print(value);
	}

	private Stmt expressionStatement() {
		Expr expr = expression();
		consume(SEMICOLON, "Expect ';' after value");

		return new Stmt.Expression(expr);
	}


	// equality → comparison ( ( "!=" | "==" ) comparison )* ;
	private Expr equality() {
		Expr expr = comparison(); // left comparison in rule

		// the while loop handles the ( ... )* in the rule
		while (match(BANG_EQUAL, EQUAL_EQUAL)) { // if we don't see one of these, we're done with the sequence of operators
			Token operator = previous();
			Expr right = comparison(); // right comparison in rule
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	// comparison → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
	// TODO: create a helper method for parsing left-associative binary operators in a reusable way
	private Expr comparison() {
		Expr expr = addition();

		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = previous();
			Expr right = addition();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr addition() {
		Expr expr = multiplication();

		while (match(MINUS, PLUS)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr multiplication() {
		Expr expr = unary();

		while (match(SLASH, STAR)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}

		return primary();
	}

	private Expr primary() {
		if (match(FALSE)) {
			return new Expr.Literal(false);
		}

		if (match(TRUE)) {
			return new Expr.Literal(true);
		}

		if (match(TokenType.NIL)) {
			return new Expr.Literal(null);
		}

		if (match(NUMBER, STRING)) { // TODO: why do we return previous here?
			return new Expr.Literal(previous().literal);
		}

		if (match(LEFT_PAREN)) {
			Expr expr = expression();

			// must have a closing parenthesis
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expr.Grouping(expr);
		}

		// if we get here, we have a token that is trying to start an expression
		throw error(peek(), "Expect expression.");
	}

	private Token advance() {
		if (!isAtEnd()) {
			current++;
		}

		return previous();
	}

	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) { // consume token and return true
				advance();
				return true;
			}
		}

		return false;
	}

	private boolean check(TokenType type) {
		if (isAtEnd()) {
			return false;
		}

		return peek().type == type;
	}

	private Token consume(TokenType type, String message) {
		if (check(type)) {
			return advance();
		}

		throw error(peek(), message);
	}

	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	// returns current token without consuming it
	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}

	// generally speaking we can synchronize on keywords and semicolons (i.e. statement boundaries)
	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			if (previous().type == SEMICOLON) return;

			switch (peek().type) {
				case CLASS:
				case FUN:
				case VAR:
				case FOR:
				case IF:
				case WHILE:
				case PRINT:
				case RETURN:
					return;
			}

			advance();
		}
	}
}
