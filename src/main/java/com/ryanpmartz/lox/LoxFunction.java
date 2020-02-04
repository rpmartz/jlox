package com.ryanpmartz.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {

	private final Stmt.Function declaration;

	public LoxFunction(Stmt.Function declaration) {
		this.declaration = declaration;
	}

	@Override
	public int arity() {
		return 0;
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		// create a new environment after each call not after each declaration (recursion)
		Environment environment = new Environment(interpreter.globals);
		for (int i = 0; i < declaration.params.size(); i++) {
			Token token = declaration.params.get(i);
			environment.define(token.lexeme, arguments.get(i));
		}

		interpreter.executeBlock(declaration.body, environment);

		return null;
	}
}
