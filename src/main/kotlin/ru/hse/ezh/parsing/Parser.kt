package ru.hse.ezh.parsing

import ru.hse.ezh.execution.Operation
import ru.hse.ezh.execution.commands.CatCommand
import ru.hse.ezh.execution.commands.EchoCommand
import ru.hse.ezh.execution.commands.PwdCommand
import ru.hse.ezh.execution.commands.WcCommand

/**
 * Parses [Token]s into [Operation]s.
 */
object Parser {

    /**
     * An immutable map from known [Operation] names to corresponding class constructors.
     */
    val keywords: Map<Token, (List<String>) -> Operation> = mapOf(
        WORD("cat") to ::CatCommand,
        WORD("echo") to ::EchoCommand,
        WORD("pwd") to ::PwdCommand,
        WORD("wc") to ::WcCommand,
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
    fun parse(tokens: List<Token>): List<Operation> = TODO("Not yet implemented")

}
