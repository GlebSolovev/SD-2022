package ru.hse.ezh.parsing

import ru.hse.ezh.exceptions.EmptyLHSException
import ru.hse.ezh.exceptions.EmptyPipeException
import ru.hse.ezh.exceptions.EmptyRHSException
import ru.hse.ezh.exceptions.NotPipedOperationsException
import ru.hse.ezh.execution.Assignment
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.Operation
import ru.hse.ezh.execution.commands.*

/**
 * Parses [Token]s into [Operation]s.
 */
object Parser {

    private enum class ParseState {
        INITIAL,
        FIRST_WORD,
        ASSIGNMENT,
        RHS,
        ARGUMENTS,
        PIPE_STARTED
    }

    /**
     * An immutable map from known [Command] names to corresponding class constructors.
     */
    val knownCommands: Map<Token, (List<String>) -> Command> = mapOf(
        WORD("cat") to ::CatCommand,
        WORD("echo") to ::EchoCommand,
        WORD("pwd") to ::PwdCommand,
        WORD("wc") to ::WcCommand,
        WORD("exit") to ::ExitCommand,
    )

    /**
     * Parses [tokens] into a list of [Operation]s.
     *
     * More specifically:
     * - validates operation syntax
     * - splits [tokens] by [PIPE] into [Operation]s
     *
     * See "Parser" automaton in scheme.pdf in root folder for details.
     *
     * @param tokens Tokens to parse. Can only contain [WORD], [ASSIGN], [PIPE].
     *
     * @return The resulting operations.
     *
     * @throws EmptyLHSException If LHS of assignment is empty.
     * @throws EmptyRHSException If RHS of assignment is empty.
     * @throws NotPipedOperationsException If consecutive operations are not separated by [PIPE].
     * @throws EmptyPipeException If [PIPE] doesn't have an operation on one side.
     */
    @Suppress("ComplexMethod", "ThrowsCount") // is ok for automata
    @Throws(
        EmptyLHSException::class,
        EmptyRHSException::class,
        NotPipedOperationsException::class,
        EmptyPipeException::class
    )
    fun parse(tokens: List<Token>): List<Operation> {

        val result = mutableListOf<Operation>()
        var state = ParseState.INITIAL

        var firstWord: WORD? = null
        val args = mutableListOf<String>()

        var lastToken: Token? = null

        fun addCommand() {
            val commandSupplier = knownCommands[firstWord!!] ?: { args -> ExternalCommand(firstWord!!.str, args) }
            val command = commandSupplier(args.toList())
            result.add(command)
            args.clear()
        }

        tokens.forEach { token ->
            state = when (state) {
                ParseState.INITIAL -> when (token) {
                    is WORD -> ParseState.FIRST_WORD.also { firstWord = token }
                    is ASSIGN -> throw EmptyLHSException(null)
                    is PIPE -> throw EmptyPipeException(null)
                    else -> throw IllegalArgumentException("unsupported token")
                }
                ParseState.FIRST_WORD -> when (token) {
                    is WORD -> ParseState.ARGUMENTS.also { args.add(token.str) }
                    is ASSIGN -> ParseState.ASSIGNMENT
                    is PIPE -> ParseState.PIPE_STARTED.also { addCommand() }
                    else -> throw IllegalArgumentException("unsupported token")
                }
                ParseState.ASSIGNMENT -> when (token) {
                    is WORD -> ParseState.RHS.also { result.add(Assignment(firstWord!!, token)) }
                    is ASSIGN, PIPE -> throw EmptyRHSException(lastToken)
                    else -> throw IllegalArgumentException("unsupported token")
                }
                ParseState.RHS -> when (token) {
                    is WORD -> throw NotPipedOperationsException(lastToken)
                    is ASSIGN -> throw EmptyLHSException(lastToken)
                    is PIPE -> ParseState.PIPE_STARTED
                    else -> throw IllegalArgumentException("unsupported token")
                }
                ParseState.ARGUMENTS -> when (token) {
                    is WORD -> state.also { args.add(token.str) }
                    is ASSIGN -> throw NotPipedOperationsException(lastToken)
                    is PIPE -> ParseState.PIPE_STARTED.also { addCommand() }
                    else -> throw IllegalArgumentException("unsupported token")
                }
                ParseState.PIPE_STARTED -> when (token) {
                    is WORD -> ParseState.FIRST_WORD.also { firstWord = token }
                    is ASSIGN -> throw EmptyLHSException(lastToken)
                    is PIPE -> throw EmptyPipeException(lastToken)
                    else -> throw IllegalArgumentException("unsupported token")
                }
            }
            lastToken = token
        }

        return when (state) {
            ParseState.INITIAL, ParseState.RHS -> result
            ParseState.FIRST_WORD, ParseState.ARGUMENTS -> result.also { addCommand() }
            ParseState.ASSIGNMENT -> throw EmptyRHSException(lastToken)
            ParseState.PIPE_STARTED -> throw EmptyPipeException(lastToken)
        }
    }

}
