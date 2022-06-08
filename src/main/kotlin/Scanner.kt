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
			'/' -> when {
				match('/') -> { while (peek() != '\n' && !isAtEnd()) advance() }
				match('*') -> comment()
				else -> addToken(SLASH)
			}
			'"' -> string()
			else -> {
				if (isDigit(c)) number()
				else if (isAlpha(c)) identifier()
				else Lox.error(line, "Unexpected character: '$c'")
			}
		}
	}

	private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z' || c == '_'

	private fun isDigit(c: Char): Boolean = c in '0'..'9'

	private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

	private fun identifier() {
		while (isAlphaNumeric(peek())) advance()
		val text = source.substring(start, current)
		val type = keywords[text] ?: IDENTIFIER
		addToken(type)
	}
	private fun comment() {
		var level = 1
		while (level > 0 && !isAtEnd()) {
			when {
				peek() == '/' && peekNext() == '*' -> level++
				peek() == '*' && peekNext() == '/' -> level--
				peek() == '\n' -> line++
			}
			advance()
		}
		advance(2)
	}

	private fun number() {
		while (isDigit(peek()))
			advance()

		// Check for fractional part
		if (peek() == '.' && isDigit(peekNext()))
			advance()

		while (isDigit(peek()))
			advance()

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
		advance()
		addToken(STRING, source.substring(start + 1, current - 1))
	}

	/**
	 * Retrieves the current character without advancing
	 */
	private fun peek(): Char {
		return if (isAtEnd()) '\u0000' else source[current]
	}

	/**
	 * Retrieves the next character without advancing
	 */
	private fun peekNext(): Char {
		return if (current + 1 >= source.length) '\u0000' else source[current + 1]
	}

	private fun isAtEnd(): Boolean {
		return current >= source.length
	}

	/**
	 * Advances if the expected character is found
	 */
	private fun match(expected: Char): Boolean {
		if (isAtEnd() || source[current] != expected) return false

		current++
		return true
	}

	/*private fun advance(): Char {
		return source[current]
	}*/

	private fun advance(by: Int = 1): Char {
		val c = current
		current += by
		return source[c]
	}

	private fun addToken(type: TokenType, literal: Any? = null) {
		tokens.add(Token(type, source.substring(start, current), literal, line))
	}
}
