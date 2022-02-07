package ru.hse.ezh.parsing

import ru.hse.ezh.exceptions.EmptyLHSException
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

    enum class ParseState {
        INITIAL,
        FIRST_WORD,
        ASSIGNMENT,
        RHS,
        ARGUMENTS
    }

    private val knownCommands: Map<Token, (List<String>) -> Command> = mapOf(
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
     */
    @Suppress("LongMethod", "ThrowsCount") // is ok for automata
    fun parse(tokens: List<Token>): List<Operation> {

        val result = mutableListOf<Operation>()
        var state = ParseState.INITIAL

        var firstWord: WORD? = null
        val args = mutableListOf<String>()

        var lastToken: Token? = null

        fun unsupportedToken(): Nothing = throw IllegalArgumentException("unsupported token")

        fun addCommand() {
            val commandSupplier = knownCommands[firstWord!!] ?: { args -> ExternalCommand(firstWord!!.str, args) }
            val command = commandSupplier(args)
            result.add(command)
        }

        tokens.forEach {
            state = when (state) {
                ParseState.INITIAL -> when (it) {
                    is WORD -> {
                        firstWord = it
                        ParseState.FIRST_WORD
                    }
                    is ASSIGN -> {
                        throw EmptyLHSException(lastToken)
                    }
                    else -> unsupportedToken()
                }
                ParseState.FIRST_WORD -> when (it) {
                    is WORD -> {
                        args.add(it.str)
                        ParseState.ARGUMENTS
                    }
                    is ASSIGN -> {
                        ParseState.ASSIGNMENT
                    }
                    else -> unsupportedToken()
                }
                ParseState.ASSIGNMENT -> when (it) {
                    is WORD -> {
                        result.add(Assignment(firstWord!!, it))
                        ParseState.RHS
                    }
                    is ASSIGN -> {
                        throw EmptyRHSException(lastToken)
                    }
                    else -> unsupportedToken()
                }
                ParseState.RHS -> when (it) {
                    is WORD -> throw NotPipedOperationsException(lastToken)
                    is ASSIGN -> throw EmptyLHSException(lastToken)
                    else -> unsupportedToken()
                }
                ParseState.ARGUMENTS -> when (it) {
                    is WORD -> {
                        args.add(it.str)
                        state
                    }
                    is ASSIGN -> {
                        throw EmptyLHSException(lastToken)
                    }
                    else -> unsupportedToken()
                }
            }
            lastToken = it
        }

        return when (state) {
            ParseState.INITIAL -> {
                result
            }
            ParseState.FIRST_WORD, ParseState.ARGUMENTS -> {
                addCommand()
                result
            }
            ParseState.ASSIGNMENT -> {
                throw EmptyRHSException(lastToken)
            }
            ParseState.RHS -> {
                result
            }
        }
    }

}
