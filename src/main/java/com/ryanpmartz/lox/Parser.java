package com.ryanpmartz.lox;

import static com.ryanpmartz.lox.TokenType.BANG_EQUAL;
import static com.ryanpmartz.lox.TokenType.EOF;
import static com.ryanpmartz.lox.TokenType.EQUAL_EQUAL;

import java.util.List;

public class Parser {

	private final List<Token> tokens;
	private int current = 0;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	// when the body of the rule contains a nonterminal - a referrence to another rule, we call that rule's method
	private Expr expression() {
		return equality();
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
}
