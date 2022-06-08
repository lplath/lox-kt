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
		run(bytes.toString())

		if (hadError) exitProcess(65)
	}

	fun runPrompt() {
		//TODO: Newlines don't work. But, I might not want them anyways
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

