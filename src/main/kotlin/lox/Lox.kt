package lox

import util.Console
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


object Lox {
	private val interpreter = Interpreter()

	private var hadError = false
	private var hadRunTimeError = false

	private fun run(source: String): String? {
		val scanner = Scanner(source)
		val tokens = scanner.scanTokens()
		val parser = Parser(tokens)
		val statements = parser.parse()

		if (hadError) return null

		return interpreter.interpret(statements)
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
			val result = run(line)

			if (result != null) Console.printResult(result)

			hadError = false
		}
	}

	private fun report(line: Int, where: String, message: String) {
		Console.printErr("Invalid Code on line $line. $message")
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
		Console.printErr("Runtime Error on line ${error.token.line}. ${error.message}")
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

