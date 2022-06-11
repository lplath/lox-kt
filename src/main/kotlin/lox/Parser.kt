package lox

import lox.TokenType.*

class ParseError : RuntimeException()

class Parser(private val tokens: List<Token>) {
	private var current = 0

	fun parse(): List<Stmt> {
		val statements = mutableListOf<Stmt>()
		while (!isAtEnd()) {
			with(declaration()) {
				if (this != null) statements.add(this)
			}
		}
		return statements
	}

	/*
	* ==================================================================================================================
	*                                                    RULES
	* ==================================================================================================================
	 */
	private fun declaration(): Stmt? {
		return try {
			when {
				match(VAR) -> varDeclaration()
				else -> statement()
			}
		} catch (error: ParseError) {
			synchronize()
			null
		}
	}

	private fun varDeclaration(): Stmt {
		val name = consume(IDENTIFIER, "Expect variable name.")
		val initializer = if (match(EQUAL)) expression() else null
		consume(SEMICOLON, "Expect ';' after variable declaration.")
		return Stmt.Var(name, initializer)
	}

	private fun statement(): Stmt {
		return when {
			match(PRINT) -> printStatement()
			match(IF) -> ifStatement()
			match(LEFT_BRACE) -> Stmt.Block(block())
			else -> expressionStatement()
		}
	}

	private fun ifStatement(): Stmt {
		consume(LEFT_PAREN, "Expect '(' after 'if'.")
		val condition = expression()
		consume(RIGHT_PAREN, "Expect ')' after if condition.")
		val thenBranch = statement()
		val elseBranch = if (match(ELSE)) statement() else null
		return Stmt.If(condition, thenBranch, elseBranch)
	}

	private fun block(): List<Stmt> {
		val statements = mutableListOf<Stmt>()
		while (!isAtEnd() && !check(RIGHT_BRACE))
			with(declaration()) {
				if (this != null) statements.add(this)
			}
		consume(RIGHT_BRACE, "Expect '}' after block.")
		return statements
	}

	private fun printStatement(): Stmt {
		val value = expression()
		consume(SEMICOLON, "Expect ';' after value.")
		return Stmt.Print(value)
	}

	private fun expressionStatement(): Stmt {
		val expr = expression()
		consume(SEMICOLON, "Expect ';' after expression.")
		return Stmt.Expression(expr)
	}

	private fun expression(): Expr {
		return assignment()
	}

	private fun assignment(): Expr {
		val expr = or()

		if (match(EQUAL)) {
			val equals = previous()
			// Allows for multiple assignments (e.g. a = b = c = 1).
			val value = assignment()

			if (expr is Expr.Variable) {
				return Expr.Assign(expr.name, value)
			}

			error(equals, "Invalid assignment target.")
		}
		return expr
	}

	private fun or(): Expr {
		var expr = and()

		while (match(OR)) {
			val operator = previous()
			val right = and()
			expr = Expr.Logical(expr, operator, right)
		}
		return expr
	}

	private fun and(): Expr {
		var expr = equality()

		while (match(AND)) {
			val operator = previous()
			val right = equality()
			expr = Expr.Logical(expr, operator, right)
		}
		return expr
	}

	private fun equality(): Expr {
		var expr = comparison()

		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			val operator = previous()
			val right = comparison()
			expr = Expr.Binary(expr, operator, right)
		}
		return expr
	}

	private fun comparison(): Expr {
		var expr = term()

		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			val operator = previous()
			val right = term()
			expr = Expr.Binary(expr, operator, right)
		}

		return expr
	}

	private fun term(): Expr {
		var expr = factor()

		while (match(MINUS, PLUS)) {
			val operator = previous()
			val right = factor()
			expr = Expr.Binary(expr, operator, right)
		}

		return expr
	}

	private fun factor(): Expr {
		var expr = unary()

		while (match(SLASH, STAR)) {
			val operator = previous()
			val right = unary()
			expr = Expr.Binary(expr, operator, right)
		}

		return expr
	}

	private fun unary(): Expr {
		if (match(BANG, MINUS)) {
			val operator = previous()
			val right = unary()
			return Expr.Unary(operator, right)
		}
		return primary()
	}

	private fun primary(): Expr {
		return when {
			match(FALSE) -> Expr.Literal(false)
			match(TRUE) -> Expr.Literal(true)
			match(NIL) -> Expr.Literal(null)
			match(NUMBER, STRING) -> Expr.Literal(previous().literal)
			match(IDENTIFIER) -> Expr.Variable(previous())
			match(LEFT_PAREN) -> {
				val expr = expression()
				consume(RIGHT_PAREN, "Expect ')' after expression.")
				return Expr.Grouping(expr)
			}
			else -> throw error(peek(), "Expect expression.")
		}
	}

	/*
	* ==================================================================================================================
	*                                              PARSER METHODS
	* ==================================================================================================================
	 */

	/**
	 * Advances if the provided type(s) match the next token(s)
	 */
	private fun match(vararg types: TokenType): Boolean {
		for (type in types) {
			if (check(type)) {
				advance()
				return true
			}
		}
		return false
	}

	/**
	 * Advances the current token
	 */
	private fun advance(): Token {
		if (!isAtEnd()) current++
		return previous()
	}

	/**
	 * Checks, if the current token matches the specified type
	 */
	private fun check(type: TokenType): Boolean {
		return !isAtEnd() && peek().type == type
	}

	private fun isAtEnd(): Boolean {
		return peek().type === EOF
	}

	/**
	 * Gets the current token without advancing
	 */
	private fun peek(): Token {
		return tokens[current]
	}

	/**
	 * Get the previous token
	 */
	private fun previous(): Token {
		return tokens[current - 1]
	}

	/**
	 * Advances, if the given token matches the following token. Throws an error otherwise.
	 */
	private fun consume(type: TokenType, message: String): Token {
		if (check(type)) return advance()

		throw error(peek(), message)
	}

	private fun error(token: Token, message: String): ParseError {
		Lox.error(token, message)
		return ParseError()
	}

	/**
	 * Advance until the end of an expression.
	 */
	private fun synchronize() {
		advance()

		while (!isAtEnd()) {
			if (previous().type == SEMICOLON) return
		}

		when (peek().type) {
			CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
		}

		advance()
	}
}