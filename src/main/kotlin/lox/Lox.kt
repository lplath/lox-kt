package lox

import util.AstPrinter
import util.RPNPrinter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


object Lox {
	val interpreter = Interpreter()

	var hadError = false
	var hadRunTimeError = false

	private fun run(source: String) {
		val scanner = Scanner(source)
		val tokens = scanner.scanTokens()
		val parser = Parser(tokens)

		val expr = parser.parse()

		if (hadError) return

		if (expr != null)
			interpreter.interpret(expr)
	}

	fun runFile(filename: String) {
		val bytes = Files.readAllBytes(Paths.get(filename))
		run(bytes.toString())

		if (hadError) exitProcess(65)
		if (hadRunTimeError) exitProcess(70)
	}

	fun runPrompt() {
		while (true) {
			print("> ")
			val line = readlnOrNull() ?: break
			run(line)
			hadError = false
		}
	}

	private fun report(line: Int, where: String, message: String) {
		System.err.println("[line $line] Error$where: $message")
		hadError = true
	}

	fun error(line: Int, message: String) = report(line, "", message)

	fun error(token: Token, message: String) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", message)
		} else {
			report(token.line, " at '${token.lexeme}'", message)
		}
	}

	fun runTimeError(error: RunTimeError) {
		System.err.println("${error.message}\n[line ${error.token.line}]")
		hadRunTimeError = true
	}
}

fun main(args: Array<String>) {
	when (args.size) {
		0 -> Lox.runPrompt()
		1 -> Lox.runFile(args.first())
		else -> {
			println("Usage: lox [script]")
		}
	}
}

