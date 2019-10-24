package com.ryanpmartz.lox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ParserTest {

	@Test
	public void testSimpleEquality() {
		String s = "5 + 7";

		Scanner scanner = new Scanner(s);
		List<Token> tokens = scanner.scanTokens();

		Parser parser = new Parser(tokens);
		Expr expr = parser.parse();

		assertTrue(expr instanceof Expr.Binary);

		Expr.Binary binary = (Expr.Binary) expr;

		assertTrue(binary.left instanceof Expr.Literal);
		assertEquals(binary.operator.type, TokenType.PLUS);
		assertTrue(binary.right instanceof Expr.Literal);

		assertEquals(5.0, ((Expr.Literal) binary.left).value);
		assertEquals(7.0, ((Expr.Literal) binary.right).value);
	}
}
