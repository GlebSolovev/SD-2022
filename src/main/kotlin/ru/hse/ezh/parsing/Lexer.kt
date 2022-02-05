package ru.hse.ezh.parsing

import ru.hse.ezh.Environment

/**
 * Splits user input into [Token]s and substitutes environment variable values.
 */
object Lexer {

    /**
     * Splits [input] into a list of tokens based on special characters (see the "Lexer" automaton
     * in scheme.pdf in the root directory).
     *
     * @param input A single user instruction as text. Must be finite.
     *
     * @return The resulting tokens.
     */
    fun lex(input: Sequence<Char>): List<Token> = TODO("Not yet implemented")

    /**
     * Finishes lexing: substitutes variable values and removes temporary tokens.
     *
     * More specifically:
     * - substitutes [QSUBST] with variable value as [WORD]
     * - substitutes [SUBST] with variable value split into [WORD]s and [SPACE]s
     * - checks syntax errors related to [SPACE]s
     * - merges consecutive [WORD]s without [SPACE] between them
     * - removes [SPACE]s
     *
     * @param tokens Tokens to process.
     * @param globalEnv Environment to take variable values from.
     *
     * @return The resulting tokens. Can only contain the following tokens: [WORD], [ASSIGN], [PIPE].
     */
    fun postprocess(tokens: List<Token>, globalEnv: Environment): List<Token> = TODO("Not yet implemented")

}
