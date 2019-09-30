package com.ryanpmartz.lox;


import static org.junit.jupiter.api.Assertions.assertEquals;

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

}