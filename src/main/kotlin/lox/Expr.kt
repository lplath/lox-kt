package lox

interface ExprVisitor<R> {
	fun visit(exp: Expr.Unary): R
	fun visit(exp: Expr.Binary): R
	fun visit(exp: Expr.Grouping): R
	fun visit(exp: Expr.Literal): R
}

sealed class Expr {
	abstract fun <R> accept(visitor: ExprVisitor<R>): R

	data class Unary(val operator: Token, val right: Expr) : Expr() {
		override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
	}

	data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
		override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
	}

	data class Grouping(val expression: Expr) : Expr() {
		override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
	}

	data class Literal(val value: Any?) : Expr() {
		override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
	}
}