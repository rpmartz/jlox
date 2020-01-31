package com.ryanpmartz.lox;

import java.util.List;

interface LoxCallable {

	Object call(Interpreter interpreter, List<Object> arguments);
}
