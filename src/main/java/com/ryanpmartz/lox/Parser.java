package com.ryanpmartz.lox;

import static com.ryanpmartz.lox.TokenType.AND;
import static com.ryanpmartz.lox.TokenType.BANG;
import static com.ryanpmartz.lox.TokenType.BANG_EQUAL;
import static com.ryanpmartz.lox.TokenType.COMMA;
import static com.ryanpmartz.lox.TokenType.ELSE;
import static com.ryanpmartz.lox.TokenType.EOF;
import static com.ryanpmartz.lox.TokenType.EQUAL;
import static com.ryanpmartz.lox.TokenType.EQUAL_EQUAL;
import static com.ryanpmartz.lox.TokenType.FALSE;
import static com.ryanpmartz.lox.TokenType.FOR;
import static com.ryanpmartz.lox.TokenType.FUN;
import static com.ryanpmartz.lox.TokenType.GREATER;
import static com.ryanpmartz.lox.TokenType.GREATER_EQUAL;
import static com.ryanpmartz.lox.TokenType.IDENTIFIER;
import static com.ryanpmartz.lox.TokenType.IF;
import static com.ryanpmartz.lox.TokenType.LEFT_BRACE;
import static com.ryanpmartz.lox.TokenType.LEFT_PAREN;
import static com.ryanpmartz.lox.TokenType.LESS;
import static com.ryanpmartz.lox.TokenType.LESS_EQUAL;
import static com.ryanpmartz.lox.TokenType.MINUS;
import static com.ryanpmartz.lox.TokenType.NUMBER;
import static com.ryanpmartz.lox.TokenType.OR;
import static com.ryanpmartz.lox.TokenType.PLUS;
import static com.ryanpmartz.lox.TokenType.PRINT;
import static com.ryanpmartz.lox.TokenType.RIGHT_BRACE;
import static com.ryanpmartz.lox.TokenType.RIGHT_PAREN;
import static com.ryanpmartz.lox.TokenType.SEMICOLON;
import static com.ryanpmartz.lox.TokenType.SLASH;
import static com.ryanpmartz.lox.TokenType.STAR;
import static com.ryanpmartz.lox.TokenType.STRING;
import static com.ryanpmartz.lox.TokenType.TRUE;
import static com.ryanpmartz.lox.TokenType.VAR;
import static com.ryanpmartz.lox.TokenType.WHILE;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
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
			statements.add(declaration());
		}

		return statements;
	}

	private Stmt declaration() {
		try {
			if (match(FUN)) {
				return function("function");
			}

			if (match(VAR)) {
				return valDeclaration();
			}

			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	private Stmt.Function function(String kind) { // used for functions and methods inside classes, hence the `kind` param
		Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

		// parse arguments
		consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
		List<Token> parameters = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (parameters.size() >= 255) {
					error(peek(), "Cannot have more than 255 parameters.");
				}

				parameters.add(consume(IDENTIFIER, "Expect parameter name."));
			} while (match(COMMA));
		}

		consume(RIGHT_PAREN, "Expect ') after " + kind + " parameter list");

		// parse function body and wrap it up in a function
		consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
		List<Stmt> body = block();
		return new Stmt.Function(name, parameters, body);
	}

	private Stmt valDeclaration() {
		Token name = consume(IDENTIFIER, "Expect variable name");

		Expr initializer = null;
		if (match(EQUAL)) {
			initializer = expression();
		}

		consume(SEMICOLON, "Expect ';' after variable declaration");
		return new Stmt.Var(name, initializer);
	}


	// when the body of the rule contains a nonterminal - a referrence to another rule, we call that rule's method
	private Expr expression() {
		return assignment();
	}

	private Expr assignment() {
		Expr expr = or();

		if (match(EQUAL)) {
			Token equals = previous();
			Expr value = assignment();

			if (expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable) expr).name;
				return new Expr.Assign(name, value);
			}

			error(equals, "Invalid assignment target.");
		}

		return expr;
	}

	private Expr or() {
		Expr expr = and();

		while (match(OR)) {
			Token operator = previous();
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr and() {
		Expr expr = equality();

		while (match(AND)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Stmt statement() {
		if (match(FOR)) {
			return forStatement();
		}

		if (match(IF)) {
			return ifStatement();
		}

		if (match(PRINT)) {
			return printStatement();
		}

		if (match(WHILE)) {
			return whileStatement();
		}

		if (match(LEFT_BRACE)) {
			return new Stmt.Block(block());
		}

		return expressionStatement();
	}

	private Stmt forStatement() {
		consume(LEFT_PAREN, "Expect '(' after 'for'.");

		Stmt initializer;
		if (match(SEMICOLON)) {  // initializer can be omitted
			initializer = null;
		} else if (match(VAR)) {
			initializer = valDeclaration();
		} else {
			initializer = expressionStatement();
		}

		Expr condition = null;
		if (!check(SEMICOLON)) {
			condition = expression(); // condition can be omitted
		}
		consume(SEMICOLON, "Expect ';' after loop condition.");


		Expr increment = null;
		if (!check(RIGHT_PAREN)) {
			increment = expression();
		}
		consume(RIGHT_PAREN, "Expect ')' after for clauses.");

		Stmt body = statement();

		if (increment != null) {
			body = new Stmt.Block(asList(body, new Stmt.Expression(increment)));
		}

		if (condition == null) {
			condition = new Expr.Literal(true);
		}

		body = new Stmt.While(condition, body);

		if (initializer != null) {
			body = new Stmt.Block(Arrays.asList(initializer, body));
		}

		return body;
	}

	private Stmt whileStatement() {
		consume(LEFT_PAREN, "Expect '(' after 'while'");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after condition in 'while'");
		Stmt body = statement();

		return new Stmt.While(condition, body);
	}

	private Stmt ifStatement() {
		consume(LEFT_PAREN, "Expect ( after if statement");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ) after conditional");

		Stmt thenBranch = statement();
		Stmt elseBranch = null;
		if (match(ELSE)) {
			elseBranch = statement();
		}

		return new Stmt.If(condition, thenBranch, elseBranch);
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

	private List<Stmt> block() {
		List<Stmt> statements = new ArrayList<>();

		while (!check(RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "Expect '}' after block.");
		return statements;
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

		return call();
	}

	// call does not match up with grammar rules perfectly
	private Expr call() {
		Expr expr = primary();

		while (true) {
			if (match(LEFT_PAREN)) {
				expr = finishCall(expr);
			} else {
				break;
			}
		}

		return expr;
	}

	// roughly the arguments grammar rule translated to code but with zero argument case
	private Expr finishCall(Expr callee) {
		List<Expr> arguments = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (arguments.size() >= 255) {
					error(peek(), "Cannot have more than 255 arguments.");
				}
				arguments.add(expression());
			} while (match(COMMA));
		}

		Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

		return new Expr.Call(callee, paren, arguments);
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

		if (match(IDENTIFIER)) {
			return new Expr.Variable(previous());
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
