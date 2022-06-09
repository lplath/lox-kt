package lox

import lox.TokenType.*

class RunTimeError(val token: Token, message: String) : RuntimeException(message)

class Interpreter : ExprVisitor<Any?> {

	fun interpret(expression: Expr) {
		try {
			val value = evaluate(expression)
			println(stringify(value))
		} catch (error: RunTimeError) {
			Lox.runTimeError(error)
		}
	}

	private fun stringify(obj: Any?): String {
		return when (obj) {
			null -> "nil"
			is Double -> {
				var text = obj.toString()
				if (text.endsWith(".0"))
					text = text.substring(0, text.length - 2)
				text
			}
			else -> obj.toString()
		}
	}

	override fun visit(exp: Expr.Unary): Any? {
		val right = evaluate(exp.right)

		return when (exp.operator.type) {
			MINUS -> -parseDouble(exp.operator, right)
			BANG -> !isTruthy(right)
			else -> null
		}
	}

	override fun visit(exp: Expr.Binary): Any? {
		val left = evaluate(exp.left)
		val right = evaluate(exp.right)

		return when (exp.operator.type) {
			GREATER -> parseDouble(exp.operator, left, right).first > parseDouble(exp.operator, left, right).second
			GREATER_EQUAL -> parseDouble(exp.operator, left, right).first >= parseDouble(exp.operator, left, right).second
			LESS -> parseDouble(exp.operator, left, right).first < parseDouble(exp.operator, left, right).second
			LESS_EQUAL -> parseDouble(exp.operator, left, right).first <= parseDouble(exp.operator, left, right).second
			EQUAL_EQUAL -> isEqual(left, right)
			BANG_EQUAL -> !isEqual(left, right)
			MINUS -> parseDouble(exp.operator, left, right).first - parseDouble(exp.operator, left, right).second
			PLUS -> when {
				left is Double && right is Double -> left + right
				left is String && right is String -> left + right
				left is String && right is Double -> left + stringify(right)
				left is Double && right is String -> stringify(left) + right
				else -> throw RunTimeError(exp.operator, "Operands must be two numbers or strings")
			}
			SLASH -> {
				val (left, right) = parseDouble(exp.operator, left, right)
				left > right
				if (right == 0.0) throw RunTimeError(exp.operator, "Cannot divide by zero.")
				else left / right
			}
			STAR -> parseDouble(exp.operator, left, right).first * parseDouble(exp.operator, left, right).second
			else -> null
		}
	}


	override fun visit(exp: Expr.Grouping): Any? {
		return evaluate(exp.expression)
	}

	override fun visit(exp: Expr.Literal): Any? {
		return exp.value
	}

	private fun evaluate(exp: Expr): Any? {
		return exp.accept(this)
	}

	@Throws(RunTimeError::class)
	private fun parseDouble(operator: Token, left: Any?, right: Any?): Pair<Double, Double> {
		if (left is Double && right is Double) return Pair(left, right)
		throw RunTimeError(operator, "Operands must be a numbers.")
	}

	@Throws(RunTimeError::class)
	private fun parseDouble(operator: Token, operand: Any?): Double {
		if (operand is Double) return operand
		throw RunTimeError(operator, "Operand must be a number.")
	}

	private fun isTruthy(obj: Any?): Boolean = when (obj) {
		null -> false
		is Boolean -> obj
		else -> true
	}

	private fun isEqual(left: Any?, right: Any?): Boolean {
		if (left == null && right == null) return true
		if (left == null) return false

		return left == right
	}
}
