package com.ryanpmartz.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {

	private final Stmt.Function declaration;
	private final Environment closure;

	public LoxFunction(Stmt.Function declaration, Environment closure) {
		this.declaration = declaration;
		this.closure = closure;
	}

	@Override
	public int arity() {
		return declaration.params.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		// create a new environment after each call not after each declaration (recursion)
		Environment environment = new Environment(closure);
		for (int i = 0; i < declaration.params.size(); i++) {
			Token token = declaration.params.get(i);
			environment.define(token.lexeme, arguments.get(i));
		}

		try {
			interpreter.executeBlock(declaration.body, environment);
		} catch (Return returnValue) {
			return returnValue.value;
		}


		return null;
	}

	@Override
	public String toString() {
		return "<fn " + declaration.name.lexeme + ">";
	}
}
