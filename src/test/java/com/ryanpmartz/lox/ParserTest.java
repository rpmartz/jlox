package com.ryanpmartz.lox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ParserTest {

	@Test
	public void testSimpleEquality() {
		String s = "5 + 7";

		Expr expr = parseToTree(s);

		assertTrue(expr instanceof Expr.Binary);

		Expr.Binary binary = (Expr.Binary) expr;

		assertTrue(binary.left instanceof Expr.Literal);
		assertEquals(binary.operator.type, TokenType.PLUS);
		assertTrue(binary.right instanceof Expr.Literal);

		assertEquals(5.0, ((Expr.Literal) binary.left).value);
		assertEquals(7.0, ((Expr.Literal) binary.right).value);
	}

	@Test
	public void testComplicatedLessThan() {
		String expression = "4 * 6 / 3 >= 10 / 2 - 1";
		Expr expr = parseToTree(expression);

		System.out.println(new AstPrinter().print(expr));
	}

	@Test
	public void testSimpleLessThan() {
		String expression = "4 <= 6";

		Expr expr = parseToTree(expression);

		assertTrue(expr instanceof Expr.Binary);

		Expr.Binary binary = ((Expr.Binary) expr);

		assertEquals(TokenType.LESS_EQUAL, binary.operator.type);
	}

	private Expr parseToTree(String expression) {
		Scanner scanner = new Scanner(expression);
		List<Token> tokens = scanner.scanTokens();

		Parser parser = new Parser(tokens);
		return parser.parse();
	}


}
