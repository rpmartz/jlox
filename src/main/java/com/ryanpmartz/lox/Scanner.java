package com.ryanpmartz.lox;

import static com.ryanpmartz.lox.TokenType.AND;
import static com.ryanpmartz.lox.TokenType.BANG;
import static com.ryanpmartz.lox.TokenType.BANG_EQUAL;
import static com.ryanpmartz.lox.TokenType.CLASS;
import static com.ryanpmartz.lox.TokenType.COMMA;
import static com.ryanpmartz.lox.TokenType.DOT;
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
import static com.ryanpmartz.lox.TokenType.NIL;
import static com.ryanpmartz.lox.TokenType.NUMBER;
import static com.ryanpmartz.lox.TokenType.OR;
import static com.ryanpmartz.lox.TokenType.PLUS;
import static com.ryanpmartz.lox.TokenType.PRINT;
import static com.ryanpmartz.lox.TokenType.RETURN;
import static com.ryanpmartz.lox.TokenType.RIGHT_BRACE;
import static com.ryanpmartz.lox.TokenType.RIGHT_PAREN;
import static com.ryanpmartz.lox.TokenType.SEMICOLON;
import static com.ryanpmartz.lox.TokenType.SLASH;
import static com.ryanpmartz.lox.TokenType.STAR;
import static com.ryanpmartz.lox.TokenType.STRING;
import static com.ryanpmartz.lox.TokenType.SUPER;
import static com.ryanpmartz.lox.TokenType.THIS;
import static com.ryanpmartz.lox.TokenType.TRUE;
import static com.ryanpmartz.lox.TokenType.VAR;
import static com.ryanpmartz.lox.TokenType.WHILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}

	private final String source;
	private final List<Token> tokens = new ArrayList<>();

	private int start = 0;
	private int current = 0;
	private int line = 1;

	public Scanner(String source) {
		if (source == null) {
			throw new IllegalArgumentException("source to parse cannot be null");
		}

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
			case '!':
				addToken(match('=') ? BANG_EQUAL : BANG);
				break;
			case '=':
				addToken(match('=') ? EQUAL_EQUAL : EQUAL);
				break;
			case '<':
				addToken(match('=') ? LESS_EQUAL : LESS);
				break;
			case '>':
				addToken(match('=') ? GREATER_EQUAL : GREATER);
				break;
			case '/':
				if (match('/')) {
					// comments go to end of line so need to consume the entire line
					while (peek() != '\n' && !isAtEnd()) { // here we don't advance if c is \n so that we increment line count later
						advance();
					}
				} else {
					addToken(SLASH);
				}
				break;
			case ' ':
			case '\r':
			case '\t':
				// Ignore whitespace
				break;
			case '\n':
				line++;
				break;
			case '"':
				string();
				break;
			default:
				// put in default case to avoid having to do cases for each numeric token
				if (isDigit(c)) {
					number();
				} else if (isAlpha(c)) {
					identifier();
				} else {
					// keep scanning when you see an unexpected character in order to both
					// avoid an infinite loop as well as ensure you show the user all errors
					// their source has
					Lox.error(line, "Unexpected character");
				}
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

	// conditionally advances if next character == expected
	private boolean match(char expected) {
		if (isAtEnd()) {
			return false;
		}

		if (source.charAt(current) != expected) {
			return false;
		}

		// conditionally advances
		current++;
		return true;
	}

	// looks ahead _without_ consuming the character
	private char peek() {
		if (isAtEnd()) {
			return '\0';
		}

		return source.charAt(current);
	}

	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') {
				// lox handles multiline strings so we need to increment line number
				line++;
			}
			advance();
		}

		// unterminated string
		if (isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}

		// move cursor to closing "
		advance();

		// trim leading and trailing quotes
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private void number() {
		// consume whole numeric literal
		while (isDigit(peek())) {
			advance();
		}

		if (peek() == '.' && isDigit(peekNext())) {
			// consume decimal but only if there are numbers after it
			advance();
		}

		// consume numbers after decimal point
		while (isDigit(peek())) {
			advance();
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	private char peekNext() {
		if (current + 1 >= source.length()) {
			return '\0';
		}

		return source.charAt(current + 1);
	}

	private void identifier() {
		// consume whole token; maximal munch means that we use the longest
		// string of characters that match
		while (isAlphaNumeric(peek())) {
			advance();
		}

		// see if token is a reserved word
		String text = source.substring(start, current);

		TokenType type = keywords.getOrDefault(text, IDENTIFIER);
		addToken(type);
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z')
				|| (c >= 'A' && c <= 'Z')
				|| c == '_';
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

}
