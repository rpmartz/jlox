package com.ryanpmartz.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

	private static final Interpreter interpreter = new Interpreter();

	static boolean hadError = false;
	static boolean hadRuntimeError = false;

	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.out.println(64); // unix sysexits.h code for incorrect usage
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		if (hadError) {
			System.exit(65);
		}

		if (hadRuntimeError) {
			System.exit(70);
		}
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (; ; ) {
			System.out.print("> ");
			run(reader.readLine());
			hadError = false;
		}
	}

	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();

		if (hadError) {
			return;
		}

		// do not run resolver if parser has errors
		Resolver resolver = new Resolver(interpreter);
		resolver.resolve(statements);

		if (hadError) {
			return;  // exit if resolution pass fails
		}

		interpreter.interpret(statements);
	}

	static void error(int line, String message) {
		report(line, "", message);
	}

	static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

	static void runtimeError(LoxRuntimeError error) {
		System.err.println(error.getMessage() +
				"\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}

	static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", message);
		} else {
			report(token.line, " at '" + token.lexeme + "'", message);
		}
	}
}
