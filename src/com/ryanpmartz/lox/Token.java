package com.ryanpmartz.lox;

public class Token {

	final TokenType type;
	final String lexeme;
	final Object literal;
	final int line;

	// group of data containing the raw lexeme along with the other things the scanner learned about it (line no, etc)
	Token(TokenType type, String lexeme, Object literal, int line) {
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
	}

	@Override
	public String toString() {
		return type + " " + lexeme + " " + literal;
	}
}
