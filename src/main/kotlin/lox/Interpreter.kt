package lox

import java.util.ArrayDeque
import lox.TokenType.*

class RunTimeError(val token: Token, message: String) : RuntimeException(message)

class Interpreter {

	private val env = ArrayDeque<Environment>()

	init {
		env.add(Environment())
	}

	@Throws(RunTimeError::class)
	fun interpret(statements: List<Stmt>): String? {
		var result: String? = null
		try {

			for (statement in statements) {
				result = execute(statement) ?: result
			}
		} catch (error: RunTimeError) {
			Lox.runTimeError(error)
		}
		return result
	}

	@Throws(RunTimeError::class)
	private fun evaluate(expr: Expr): Any? {
		return when (expr) {
			is Expr.Literal -> expr.value
			is Expr.Unary -> {
				val right = evaluate(expr.right)

				return when (expr.operator.type) {
					MINUS -> -parseDouble(expr.operator, right)
					BANG -> !isTruthy(right)
					else -> null
				}
			}
			is Expr.Binary -> {
				val left = evaluate(expr.left)
				val right = evaluate(expr.right)

				return when (expr.operator.type) {
					GREATER -> parseDouble(expr.operator, left, right).first > parseDouble(expr.operator, left, right).second
					GREATER_EQUAL -> parseDouble(expr.operator, left, right).first >= parseDouble(
						expr.operator,
						left,
						right
					).second
					LESS -> parseDouble(expr.operator, left, right).first < parseDouble(expr.operator, left, right).second
					LESS_EQUAL -> parseDouble(expr.operator, left, right).first <= parseDouble(
						expr.operator,
						left,
						right
					).second
					EQUAL_EQUAL -> isEqual(left, right)
					BANG_EQUAL -> !isEqual(left, right)
					MINUS -> parseDouble(expr.operator, left, right).first - parseDouble(expr.operator, left, right).second
					PLUS -> when {
						left is Double && right is Double -> left + right
						left is String && right is String -> left + right
						left is String && right is Double -> left + stringify(right)
						left is Double && right is String -> stringify(left) + right
						else -> throw RunTimeError(expr.operator, "Operands must be two numbers or strings")
					}
					SLASH -> {
						val (left, right) = parseDouble(expr.operator, left, right)
						left > right
						if (right == 0.0) throw RunTimeError(expr.operator, "Cannot divide by zero.")
						else left / right
					}
					STAR -> parseDouble(expr.operator, left, right).first * parseDouble(expr.operator, left, right).second
					else -> null
				}
			}
			is Expr.Grouping -> evaluate(expr.expression)
			is Expr.Logical -> {
				val left = evaluate(expr.left)
				when (expr.operator.type) {
					OR -> if (isTruthy(left)) return left
					AND -> if(!isTruthy(left)) return left
					else -> {}
				}
				return evaluate(expr.right)
			}
			is Expr.Assign -> {
				val value = evaluate(expr.value)
				env.peek().assign(expr.name, value)
				env
				return value
			}
			is Expr.Variable -> {
				val result = env.peek().get(expr.name)
				if (result != null) return result
				else throw RunTimeError(expr.name, "Variable '${expr.name.lexeme}' has not been initialized.")
			}
		}
	}

	private fun execute(stmt: Stmt): String? {
		return when (stmt) {
			is Stmt.Block -> {
				var result: String? = null
				try {
					env.push(Environment(this.env.peek()))
					for (statement in stmt.statements) {
						result = execute(statement) ?: result
					}
				} finally {
					this.env.pop()
				}
				return result
			}
			is Stmt.If -> {
				if (isTruthy(stmt.condition)) {
					execute(stmt.thenBranch)
				} else if (stmt.elseBranch != null) {
					execute(stmt.elseBranch)
				}
				return null
			}
			is Stmt.Var -> {
				val value = if (stmt.initializer != null) evaluate(stmt.initializer) else null
				env.peek().define(stmt.name.lexeme, value)
				return null
			}
			is Stmt.Print -> {
				val value = evaluate(stmt.expression)
				println(stringify(value))
				return null
			}
			is Stmt.Expression -> stringify(evaluate(stmt.expression))
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
