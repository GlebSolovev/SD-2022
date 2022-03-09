package ru.hse.ezh

import ru.hse.ezh.exceptions.*
import ru.hse.ezh.execution.Executor
import ru.hse.ezh.execution.Operation
import ru.hse.ezh.execution.commands.utils.CHARSET
import ru.hse.ezh.parsing.Lexer
import ru.hse.ezh.parsing.Parser
import ru.hse.ezh.views.ConsoleView
import ru.hse.ezh.views.View

import java.io.ByteArrayInputStream

import kotlin.system.exitProcess

/**
 * Main Ezh shell class.
 *
 * Responsible for launching the program, controlling its lifecycle and terminating it.
 *
 * Holds the session global environment.
 *
 * @constructor
 * @param view The [View] to interact with user.
 */
class Ezh(private val view: View = ConsoleView()) {

    private val globalEnv: Environment = Environment()

    /**
     * Ezh entry point: runs the Ezh shell.
     *
     * This method repeats the following steps in a cycle:
     * - gets user input using a [View]
     * - splits the input into tokens using [Lexer]
     * - parses the tokens into [Operation]s using [Parser]
     * - executes the operations using [Executor]
     * - prints the output of operations using a [View]
     *
     * The cycle is broken with an 'exit' command.
     *
     * @return
     * - The status code with which the 'exit' command was called.
     * - -1 if interaction with user failed
     */
    fun main(): Int {
        fun View.writeErrorString(s: String) = this.writeError(ByteArrayInputStream((s + "\n").toByteArray(CHARSET)))

        while (true) {
            try {
                val inputSequence = view.getInput()
                val lexedTokens = Lexer.postprocess(Lexer.lex(inputSequence), globalEnv)
                val parsedOperations = Parser.parse(lexedTokens)
                val (code, out, err) = Executor.execute(parsedOperations, globalEnv)
                if (globalEnv.exitStatus == Environment.ExitStatus.EXITING) {
                    return code
                }
                view.writeOutput(out)
                view.writeError(err)
            } catch (_: ViewException) {
                return -1 // cannot interact with user anymore, exiting
            } catch (e: LexerException) {
                view.writeErrorString("lexing error: ${e.message}")
            } catch (e: ParserException) {
                view.writeErrorString("parsing error: ${e.message}")
            } catch (e: CommandStartupException) {
                view.writeErrorString(e.message!!)
            } catch (e: ExecutionIOException) {
                view.writeErrorString(
                    "obscure execution error\ncause: ${e.message}\n" +
                        "maybe try running that command again?"
                )
            }
        }
    }

}

fun main() {
    exitProcess(Ezh().main())
}
