package com.ryanpmartz.lox;

import static com.ryanpmartz.lox.TokenType.BANG_EQUAL;
import static com.ryanpmartz.lox.TokenType.EOF;
import static com.ryanpmartz.lox.TokenType.EQUAL_EQUAL;
import static com.ryanpmartz.lox.TokenType.GREATER;
import static com.ryanpmartz.lox.TokenType.GREATER_EQUAL;
import static com.ryanpmartz.lox.TokenType.LESS;
import static com.ryanpmartz.lox.TokenType.LESS_EQUAL;
import static com.ryanpmartz.lox.TokenType.MINUS;
import static com.ryanpmartz.lox.TokenType.SLASH;
import static com.ryanpmartz.lox.TokenType.STAR;

import java.util.List;

public class Parser {

	private final List<Token> tokens;
	private int current = 0;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	// first rule in our grammar is expression that translates to equality
	private Expr expression() {
		return equality();
	}

	// equality → comparison ( ( "!=" | "==" ) comparison )* ;
	private Expr equality() {
		Expr expr = comparison(); // left comparison nonterminal

		// need the loop for the ( ... )* part of the rule. consider:
		// a == b == c == d == e. Then each time through
		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	// comparison → addition ( ( ">" | ">=" | "<" | "<=" ) addition )*
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

		while (match(MINUS, TokenType.PLUS)) {
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

	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
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

	private Token advance() {
		if (!isAtEnd()) {
			current++;
		}
		return previous();
	}

	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}
}
