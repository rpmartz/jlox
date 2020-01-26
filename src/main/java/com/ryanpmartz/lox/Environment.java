package com.ryanpmartz.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {

	private final Map<String, Object> values = new HashMap<>();

	public void define(String name, Object value) {
		// note that there's no check whether variable here is already defined,
		// i.e. variables are mutable
		values.put(name, value);
	}

	public Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		throw new LoxRuntimeError(name, "Undefined variable '" + name.lexeme + "'. ");
	}

}
