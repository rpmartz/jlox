package com.ryanpmartz.lox;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class ScannerTest {

	@Test
	public void testTwoBrackets() {
		String source = "{}";
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		for (Token token : tokens) {
			System.out.println(token);
		}

		assertEquals(3, tokens.size());
	}

	@Test
	public void testMultilineInput() {
		String source = "{\n\n}";
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		assertEquals(3, tokens.size());
	}

	@Test
	public void testSingleBangOperator() {
		String source = "!a";
		Scanner scanner = new Scanner(source);

		List<Token> tokens = scanner.scanTokens();
		Token token = tokens.get(0);

		assertEquals(token.type, TokenType.BANG);
	}

	@Test
	public void testNotEqual() {
		String source = "!=";
		Scanner scanner = new Scanner(source);

		List<Token> tokens = scanner.scanTokens();
		Token token = tokens.get(0);

		assertEquals(token.type, TokenType.BANG_EQUAL);
	}

	@Test
	public void testInvalidComparison() {
		String source = "\"a\" > 5";
		List<Token> tokens = new Scanner(source).scanTokens();

		Token firstToken = tokens.get(0);
		assertEquals(TokenType.STRING, firstToken.type);
		assertEquals("\"a\"", firstToken.lexeme);
		assertEquals("a", firstToken.literal);

		Token secondToken = tokens.get(1);
		assertEquals(TokenType.GREATER, secondToken.type);
		assertEquals(">", secondToken.lexeme);
		assertNull(secondToken.literal);

		Token thirdToken = tokens.get(2);
		assertEquals(TokenType.NUMBER, thirdToken.type);
		assertEquals("5", thirdToken.lexeme);
		assertEquals(5.0, thirdToken.literal);
	}

	@Test
	public void testMultiLine() {
		String source = "{var dog = \"Bandit\";\nprint dog;}";
		List<Token> tokens = new Scanner(source).scanTokens();
		for (Token token : tokens) {
			System.out.println(token);
		}
	}

}