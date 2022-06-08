import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


object Lox {
	var hadError = false

	private fun run(source: String) {
		val scanner = Scanner(source)
		val tokens = scanner.scanTokens()

		for (t in tokens) {
			println(t)
		}
	}

	fun runFile(filename: String) {
		val bytes = Files.readAllBytes(Paths.get(filename))
		run(String(bytes, Charset.defaultCharset()))

		if (hadError) exitProcess(65)
	}

	fun runPrompt() {
		val input = InputStreamReader(System.`in`)
		val reader = BufferedReader(input)

		while (true) {
			println("> ")
			val line = reader.readLine() ?: break
			run(line)
			hadError = false
		}
	}

	fun report(line: Int, where: String, message: String) {
		System.err.println("[line $line] Error$where: $message")
		hadError = true
	}

	fun error(line: Int, message: String) = report(line, "", message)
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

