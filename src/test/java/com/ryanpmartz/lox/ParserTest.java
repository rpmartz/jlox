package com.ryanpmartz.lox;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ryanpmartz.lox.tool.StatementPrinter;

public class ParserTest {

	@Test
	public void testSimpleEquality() {
		String s = "5 + 7;";

		List<Stmt> statements = parseToTree(s);

		for (Stmt statement : statements) {
			StatementPrinter printer = new StatementPrinter();
			printer.print(statement);
		}

//		assertTrue(expr instanceof Expr.Binary);
//
//		Expr.Binary binary = (Expr.Binary) expr;
//
//		assertTrue(binary.left instanceof Expr.Literal);
//		assertEquals(binary.operator.type, TokenType.PLUS);
//		assertTrue(binary.right instanceof Expr.Literal);
//
//		assertEquals(5.0, ((Expr.Literal) binary.left).value);
//		assertEquals(7.0, ((Expr.Literal) binary.right).value);
	}

	@Test
	public void testComplicatedLessThan() {
		String expression = "4 * 6 / 3 >= 10 / 2 - 1;";
	}

	private List<Stmt> parseToTree(String expression) {
		Scanner scanner = new Scanner(expression);
		List<Token> tokens = scanner.scanTokens();

		Parser parser = new Parser(tokens);
		return parser.parse();
	}


}
