import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Lox {

    private var hadError = false

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: jlox [script]")
            // EX_USAGE: The command was used incorrectly
            // https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
            exitProcess(64)
        } else if (args.size == 1) {
            // reads the file and executes it
            runFile(args[0])
        } else {
            // run interactively, enter and execute one line at a time
            runPrompt()
        }
    }

    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        if (hadError) {
            //  EX_DATAERR: The input data was incorrect in some way.
            exitProcess(65)
        }
    }

    private fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            // [repl]
            print("> ")
            val line = reader.readLine() ?: break
            run(line)

            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        for (token in tokens) {
            println(token)
        }

    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println(
            "[line $line] Error$where: $message"
        )
        hadError = true
    }
}