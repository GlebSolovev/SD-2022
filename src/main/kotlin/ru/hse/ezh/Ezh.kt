package ru.hse.ezh

import kotlin.system.exitProcess
import ru.hse.ezh.parsing.Lexer
import ru.hse.ezh.parsing.Parser
import ru.hse.ezh.views.ConsoleView
import ru.hse.ezh.views.View
import ru.hse.ezh.execution.Operation
import ru.hse.ezh.execution.Executor

/**
 * Main Ezh shell class.
 *
 * Responsible for launching the program, controlling its lifecycle and terminating it.
 *
 * Holds the session global environment.
 *
 * Currently, only supports [ConsoleView].
 */
class Ezh {

    private val view: View = ConsoleView()
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
     * @return The status code with which the 'exit' command was called.
     */
    fun main(): Int = TODO("Not yet implemented")

}

fun main() {
    exitProcess(Ezh().main())
}
