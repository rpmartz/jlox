package com.ryanpmartz.lox;

import static com.ryanpmartz.lox.TokenType.COMMA;
import static com.ryanpmartz.lox.TokenType.DOT;
import static com.ryanpmartz.lox.TokenType.EOF;
import static com.ryanpmartz.lox.TokenType.LEFT_BRACE;
import static com.ryanpmartz.lox.TokenType.LEFT_PAREN;
import static com.ryanpmartz.lox.TokenType.MINUS;
import static com.ryanpmartz.lox.TokenType.PLUS;
import static com.ryanpmartz.lox.TokenType.RIGHT_BRACE;
import static com.ryanpmartz.lox.TokenType.RIGHT_PAREN;
import static com.ryanpmartz.lox.TokenType.SEMICOLON;
import static com.ryanpmartz.lox.TokenType.STAR;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

	private final String source;
	private final List<Token> tokens = new ArrayList<>();

	private int start = 0;
	private int current = 0;
	private int line = 1;

	public Scanner(String source) {
		this.source = source;
	}

	protected List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	// each turn of the loop, scan a single token
	private void scanToken() {
		char c = advance();
		switch (c) {
			case '(':
				addToken(LEFT_PAREN);
				break;
			case ')':
				addToken(RIGHT_PAREN);
				break;
			case '{':
				addToken(LEFT_BRACE);
				break;
			case '}':
				addToken(RIGHT_BRACE);
				break;
			case ',':
				addToken(COMMA);
				break;
			case '.':
				addToken(DOT);
				break;
			case '-':
				addToken(MINUS);
				break;
			case '+':
				addToken(PLUS);
				break;
			case ';':
				addToken(SEMICOLON);
				break;
			case '*':
				addToken(STAR);
				break;
			default:
				// keep scanning when you see an unexpected character in order to both
				// avoid an infinite loop as well as ensure you show the user all errors
				// their source has
				Lox.error(line, "Unexpected character");
				break;
		}
	}

	private char advance() {
		current++;
		return source.charAt(current - 1);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
}
