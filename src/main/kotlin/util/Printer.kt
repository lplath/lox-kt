package util

import lox.Expr
import lox.ExprVisitor
import lox.Token
import lox.TokenType

class AstPrinter : ExprVisitor<String> {
	fun print(expr: Expr?): String {
		return expr?.accept(this) ?: "[NULL]"
	}

	private fun parenthesize(name: String, vararg exprs: Expr): String {
		val builder = StringBuilder()
		builder.append("($name")
		for (expr in exprs) {
			builder.append(" ${expr.accept(this)}")
		}
		builder.append(")")

		return builder.toString()
	}

	override fun visit(exp: Expr.Unary) = parenthesize(exp.operator.lexeme, exp.right)

	override fun visit(exp: Expr.Binary) = parenthesize(exp.operator.lexeme, exp.left, exp.right)

	override fun visit(exp: Expr.Grouping) = parenthesize("group", exp.expression)

	override fun visit(exp: Expr.Literal) = exp.value?.toString() ?: "nil"
}


/*
	Exercise 2. & 3. Implement the printing in a functional style
 */
class RPNPrinter {
	fun print(e: Expr?): String = when (e) {
		is Expr.Unary -> "${print(e.right)} ${e.operator.lexeme}"
		is Expr.Binary -> "${print(e.left)} ${print(e.right)} ${e.operator.lexeme}"
		is Expr.Literal -> e.value?.toString() ?: "nil"
		is Expr.Grouping -> print(e.expression)
		null -> "[NULL]"
	}
}

fun main() {
	// (1 + 2) * (4 - 3)
	val example = Expr.Binary(
		Expr.Grouping(
			Expr.Binary(
				Expr.Literal(1),
				Token(TokenType.PLUS, "+", null, 1),
				Expr.Literal(2)
			)
		),
		Token(TokenType.STAR, "*", null, 1),
		Expr.Grouping(
			Expr.Binary(
				Expr.Literal(4),
				Token(TokenType.STAR, "-", null, 1),
				Expr.Literal(3)
			)
		)
	)
	// expect: 1 2 + 4 3 - *
	println(RPNPrinter().print(example))
}