import TokenType.*

class Scanner(private val source: String) {
	companion object {
		val keywords = hashMapOf(
			"and"       to AND,
			"class"     to CLASS,
			"else"      to ELSE,
			"false"     to FALSE,
			"for"       to FOR,
			"fun"       to FUN,
			"if"        to IF,
			"nil"       to NIL,
			"or"        to OR,
			"print"     to PRINT,
			"return"    to RETURN,
			"super"     to SUPER,
			"this"      to THIS,
			"true"      to TRUE,
			"var"       to VAR,
			"while"     to WHILE
		)
	}


	private val tokens = mutableListOf<Token>()
	private var start = 0
	private var current = 0
	private var line = 1

	fun scanTokens(): List<Token> {
		while (!isAtEnd()) {
			start = current
			scanToken()
		}
		tokens.add(Token(EOF, "", null, line))
		return tokens
	}

	private fun scanToken() {
		when (val c = advance()) {
			' ', '\r', '\t' -> {}
			'\n' -> line++
			'(' -> addToken(LEFT_PAREN)
			')' -> addToken(RIGHT_PAREN)
			'{' -> addToken(LEFT_BRACE)
			'}' -> addToken(RIGHT_BRACE)
			',' -> addToken(COMMA)
			'.' -> addToken(DOT)
			'-' -> addToken(MINUS)
			'+' -> addToken(PLUS)
			';' -> addToken(SEMICOLON)
			'*' -> addToken(STAR)
			'!' -> if (match('=')) addToken(BANG_EQUAL) else addToken(BANG)
			'=' -> if (match('=')) addToken(EQUAL_EQUAL) else addToken(EQUAL)
			'<' -> if (match('=')) addToken(LESS_EQUAL) else addToken(LESS)
			'>' -> if (match('=')) addToken(GREATER_EQUAL) else addToken(GREATER)
			'/' -> if (match('/')) { while (peek() != '\n' && !isAtEnd()) advance() } else addToken(SLASH)
			'"' -> string()
			else -> {
				if (isDigit(c)) number()
				else if (isAlpha(c)) identifier()
				else Lox.error(line, "Unexpected character")
			}
		}
	}


	private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z' || c == '_'

	private fun isDigit(c: Char): Boolean = c in '0'..'9'

	private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

	private fun identifier() {
		while (isAlphaNumeric(peek())) advance()
		val text = source.substring(start, current)
		var type = keywords[text]
		if (type == null) type = IDENTIFIER
		addToken(type)
	}

	private fun number() {
		while (isDigit(peek())) advance()

		// Check for fractional part
		if (peek() == '.' && isDigit(peekNext()))
			advance()

		while (isDigit(peek())) advance()

		addToken(NUMBER, source.substring(start, current).toDouble())
	}


	private fun string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') line++
			advance()
		}

		if (isAtEnd()) {
			Lox.error(line, "Unterminated string")
			return
		}

		// closing "
		advance()

		addToken(STRING, source.substring(start + 1, current - 1))
	}

	/**
	 * Retrieves the current character without advancing
	 */
	private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]

	/**
	 * Retrieves the next character without advancing
	 */
	private fun peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1]



	private fun isAtEnd(): Boolean = current >= source.length

	/**
	 * Advances until the expected character is found
	 */
	private fun match(expected: Char): Boolean {
		if (isAtEnd() && source[current] != expected) return false

		current++
		return true
	}

	private fun advance(): Char = source[current++]

	private fun addToken(type: TokenType, literal: Any? = null) {
		tokens.add(Token(type, source.substring(start, current), literal, line))
	}
}
