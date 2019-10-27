package com.ryanpmartz.lox;

public class Interpreter implements Expr.Visitor<Object> {

	// a literal tree node just translates to a literal runtime value
	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}

	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary expr) {
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
			case MINUS:
				return -(double) right;
			case BANG:
				return !isTruthy(right);
		}

		// Unreachable.
		return null;
	}

	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
			case GREATER:
				return (double) left > (double) right;
			case GREATER_EQUAL:
				return (double) left >= (double) right;
			case LESS:
				return (double) left < (double) right;
			case LESS_EQUAL:
				return (double) left <= (double) right;
			case MINUS:
				checkNumberOperand(expr.operator, right);
				return (double) left - (double) right;
			case BANG_EQUAL:
				return !isEqual(left, right);
			case EQUAL_EQUAL:
				return isEqual(left, right);
			case PLUS:
				if (left instanceof Double && right instanceof Double) {
					return (double) left + (double) right;
				}

				if (left instanceof String && right instanceof String) {
					return (String) left + (String) right;
				}
			case SLASH:
				return (double) left / (double) right;
			case STAR:
				return (double) left * (double) right;

		}

		// Unreachable
		return null;
	}

	private boolean isTruthy(Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof Boolean) {
			return (boolean) object;
		}

		return true;
	}

	private boolean isEqual(Object a, Object b) {
		// nil is only equal ot nil
		if (a == null && b == null) {
			return true;
		}
		if (a == null) {
			return false;
		}

		return a.equals(b);
	}

	private void checkNumberOperand(Token operator, Object operand) {
		boolean isDouble = operand instanceof Double;
		if (!isDouble) {
			throw new LoxRuntimeError(operator, "Operand must be a number");
		}
	}
}
