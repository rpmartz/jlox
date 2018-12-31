package com.ryanpmartz.lox;

import static com.ryanpmartz.lox.Logger.log;
import static com.ryanpmartz.lox.TokenType.BANG;
import static com.ryanpmartz.lox.TokenType.BANG_EQUAL;
import static com.ryanpmartz.lox.TokenType.COMMA;
import static com.ryanpmartz.lox.TokenType.DOT;
import static com.ryanpmartz.lox.TokenType.EOF;
import static com.ryanpmartz.lox.TokenType.EQUAL;
import static com.ryanpmartz.lox.TokenType.EQUAL_EQUAL;
import static com.ryanpmartz.lox.TokenType.GREATER;
import static com.ryanpmartz.lox.TokenType.GREATER_EQUAL;
import static com.ryanpmartz.lox.TokenType.IDENTIFIER;
import static com.ryanpmartz.lox.TokenType.LEFT_BRACE;
import static com.ryanpmartz.lox.TokenType.LEFT_PAREN;
import static com.ryanpmartz.lox.TokenType.LESS;
import static com.ryanpmartz.lox.TokenType.LESS_EQUAL;
import static com.ryanpmartz.lox.TokenType.MINUS;
import static com.ryanpmartz.lox.TokenType.NUMBER;
import static com.ryanpmartz.lox.TokenType.PLUS;
import static com.ryanpmartz.lox.TokenType.RIGHT_BRACE;
import static com.ryanpmartz.lox.TokenType.RIGHT_PAREN;
import static com.ryanpmartz.lox.TokenType.SEMICOLON;
import static com.ryanpmartz.lox.TokenType.SLASH;
import static com.ryanpmartz.lox.TokenType.STAR;
import static com.ryanpmartz.lox.TokenType.STRING;

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

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// we are at beginning of next lexeme
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));

		return tokens;
	}

	private boolean isAtEnd() {
		log("Scanner.isAtEnd() invoked", this);
		return current >= source.length();
	}

	private void scanToken() {
		log("Scanner.scanToken() invoked", this);
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
			case '!':
				addToken(nextCharMatches('=') ? BANG_EQUAL : BANG);
				break;
			case '=':
				addToken(nextCharMatches('=') ? EQUAL_EQUAL : EQUAL);
				break;
			case '<':
				addToken(nextCharMatches('=') ? LESS_EQUAL : LESS);
				break;
			case '>':
				addToken(nextCharMatches('=') ? GREATER_EQUAL : GREATER);
				break;
			case '/':
				if (nextCharMatches('/')) {
					// A comment goes until the end of the line.
					while (peek() != '\n' && !isAtEnd()) {
						advance();
					}

					// note that we don't add a Token for a comment
				} else {
					addToken(SLASH);
				}
				break;
			case ' ':
			case '\r':
			case '\t':
				break; // ignore whitespace

			case '\n':
				line++;
				break;

			case '"':
				string();
				break;
			default:
				if (isDigit(c)) {
					number();
				} else if (isAlpha(c)) {
					identifier();
				} else {
					Lox.error(line, "Unexpected character.");
				}
				break;
		}
	}

	private char advance() {
		log("Scanner.advance() invoked", this);
		current++;
		return source.charAt(current - 1);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		log("Scanner.addToken() invoked. Adding [" + literal + "] as type [" + type + "]", this);
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	// method to figure out if a token is a compound token, e.g. = or ==
	// in this book this is called nextCharMatches but I'm renaming
	private boolean nextCharMatches(char expected) {
		log("Scanner.nextCharMatches() checking for [" + expected + "]", this);
		if (isAtEnd()) {
			return false;
		}

		if (source.charAt(current) != expected) {
			return false;
		}

		current++;
		return true;
	}

	private void identifier() {
		while (isAlphaNumeric(peek())) {
			advance();
		}

		addToken(IDENTIFIER);
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z') ||
				c == '_';
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private char peek() {
		log("Scanner.peek() invoked", this);
		if (isAtEnd()) {
			return '\0';
		}

		return source.charAt(current);
	}

	private void string() {
		log("Scanner.string() invoked", this);
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') { // support multiline strings
				line++;
			}
			advance();
		}

		if (isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}

		// advance to closing '"'
		advance();

		// trim surrounding quotes
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	private boolean isDigit(char c) {
		// allow 1234 and 12.34 but not .1234 or 1234.
		// .1234 is easy but 1234. makes methods on numbers, e.g. 12.sqrt() hard
		return c >= '0' && c <= '9';
	}

	private void number() {
		log("Scanner.number() invoked", this);
		while (isDigit(peek())) {
			advance();
		}

		// Look for a fractional part.
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();

			while (isDigit(peek())) {
				advance();
			}
		}

		addToken(NUMBER,
				Double.parseDouble(source.substring(start, current)));
	}

	private char peekNext() {
		log("Scanner.peekNext() invoked", this);
		if (current + 1 >= source.length()) {
			return '\0';
		}

		return source.charAt(current + 1);
	}

	@Override
	public String toString() {
		return "Scanner{" +
				"source='" + source + '\'' +
				", tokens=" + tokens +
				", start=" + start +
				", current=" + current +
				", line=" + line +
				'}';
	}
}
