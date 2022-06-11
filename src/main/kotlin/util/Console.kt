package util

object Console {

	private const val RED = "\u001B[31m"
	private const val GRAY = "\u001B[37m"
	private const val RESET = "\u001B[0m"

	fun printErr(msg: String) = println(RED + msg + RESET)
	fun printResult(msg: String) = println(GRAY + msg + RESET)
}