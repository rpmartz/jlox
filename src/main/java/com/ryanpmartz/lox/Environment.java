package com.ryanpmartz.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {

	final Environment enclosing; // reference to parent
	private final Map<String, Object> values = new HashMap<>();

	public Environment() {
		enclosing = null;
	}

	Environment(Environment env) {
		this.enclosing = env;
	}

	public void define(String name, Object value) {
		// note that there's no check whether variable here is already defined,
		// i.e. variables are mutable
		values.put(name, value);
	}

	public Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		if (enclosing != null) {
			return enclosing.get(name);
		}

		throw new LoxRuntimeError(name, "Undefined variable '" + name.lexeme + "'. ");
	}

	public void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
			return;
		}

		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}

		throw new LoxRuntimeError(name, "Undefined variable '" + name.lexeme + "'. ");
	}

	public void assignAt(int distance, Token name, Object value) {
		ancestor(distance).values.put(name.lexeme, value);
	}

	public Object getAt(int distance, String name) {
		return ancestor(distance).values.get(name);
	}

	public Environment ancestor(int distance) {
		// here we are assuming the Resolver did its job and can
		// hop directly to the enclosing environment,
		// but this is a tight logical coupling - could use asserts
		Environment environment = this;
		for (int i = 0; i < distance; i++) {
			environment = environment.enclosing;
		}

		return environment;
	}


}
