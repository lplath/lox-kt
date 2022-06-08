import lox.Scanner
import lox.TokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ScannerTest {

	@Test
	fun `comments | empty produces EOF`() {
		val tokens = Scanner("").scanTokens()

		assertEquals(1, tokens.size)
		assertEquals(TokenType.EOF, tokens.first().type)
	}

	@Test
	fun `comments | single slash produces SLASH`() {
		val tokens = Scanner("1 / 2").scanTokens()

		assertEquals(TokenType.SLASH, tokens[1].type)
	}

	@Test
	fun `comments | single line produces EOF`(){
		val tokens = Scanner("// var a = 123").scanTokens()

		assertEquals(1, tokens.size)
		assertEquals(TokenType.EOF, tokens.first().type)
	}

	@Test
	fun `comments | new line produces result`() {
		val tokens = Scanner("// var a = 123 \n var b = 2").scanTokens()

		assertEquals(TokenType.VAR, tokens.first().type)
		assertEquals(2, tokens.first().line)
	}

	@Test
	fun `comments | multiline produces EOF`() {
		val tokens = Scanner("/* var a = 123 */").scanTokens()

		assertEquals(1, tokens.size)
		assertEquals(TokenType.EOF, tokens.first().type)
	}

	@Test
	fun`comments | multiline produces result`() {
		val tokens = Scanner("/* ignore this */ var a = 123 ").scanTokens()

		assertEquals(TokenType.VAR, tokens.first().type)
	}

	@Test
	fun `comments | multiline produces result over multiple line`() {
		val tokens = Scanner("/* ignore this \n And this too */ var a = 123 ").scanTokens()

		assertEquals(TokenType.VAR, tokens.first().type)
		assertEquals(2, tokens.first().line)
	}

	@Test
	fun `comments | nested multiline comments are ignored`() {
		val tokens = Scanner("/* ignore this /* and this too */ ... and also this */").scanTokens()

		assertEquals(TokenType.EOF, tokens.first().type)
	}

	@Test
	fun`strings | basic`() {
		val tokens = Scanner("\"hello\"").scanTokens()

		assertEquals(TokenType.STRING, tokens.first().type)
	}

	@Test
	fun`strings | two strings`() {
		val tokens = Scanner("\"hello\" not_a_string \"world\"").scanTokens()

		assertEquals(TokenType.STRING, tokens[0].type)
		assertEquals(TokenType.STRING, tokens[2].type)
	}

	@Test
	fun`strings | multiline strings`() {
		val tokens = Scanner("\"hello \n world\"").scanTokens()

		assertEquals(TokenType.STRING, tokens[0].type)
		assertEquals(TokenType.EOF, tokens[1].type)
	}
}