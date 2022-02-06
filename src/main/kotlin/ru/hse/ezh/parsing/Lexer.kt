package ru.hse.ezh.parsing

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.UnterminatedQuotesException

import java.lang.Character.isWhitespace

import kotlin.jvm.Throws

/**
 * Splits user input into [Token]s and substitutes environment variable values.
 */
object Lexer {

    private enum class LexState {
        UNQUOTED_WORD,
        WHITESPACE,
        FULLY_QUOTED_WORD,
        WEAKLY_QUOTED_WORD,
    }

    /**
     * Splits [input] into a list of tokens based on special characters (see the "Lexer" automaton
     * in scheme.pdf in the root directory).
     *
     * @param input A single user instruction as text. Must be finite.
     *
     * @return The resulting tokens.
     *
     * @throws UnterminatedQuotesException When an unterminated quote is encountered.
     */
    @Throws(UnterminatedQuotesException::class)
    fun lex(input: Sequence<Char>): List<Token> {
        val result = mutableListOf<Token>()
        var state = LexState.UNQUOTED_WORD
        val rawToken = StringBuilder()
        var position = 0
        input.forEach {
            position++
            state = when (state) {
                LexState.UNQUOTED_WORD -> {
                    when (it) {
                        '\'' -> LexState.FULLY_QUOTED_WORD
                        '\"' -> LexState.WEAKLY_QUOTED_WORD
                        else -> {
                            if (isWhitespace(it)) {
                                if (rawToken.isNotEmpty()) result.add(WORD(rawToken.toString()))
                                rawToken.clear()
                                result.add(SPACE)
                                LexState.WHITESPACE
                            } else {
                                rawToken.append(it)
                                state
                            }
                        }
                    }
                }
                LexState.WHITESPACE -> when (it) {
                    '\'' -> LexState.FULLY_QUOTED_WORD
                    '\"' -> LexState.WEAKLY_QUOTED_WORD
                    else -> {
                        if (isWhitespace(it)) {
                            state
                        } else {
                            rawToken.append(it)
                            LexState.UNQUOTED_WORD
                        }
                    }
                }
                LexState.FULLY_QUOTED_WORD -> when (it) {
                    '\'' -> {
                        if (rawToken.isEmpty()) result.add(WORD(""))
                        LexState.UNQUOTED_WORD
                    }
                    else -> {
                        rawToken.append(it)
                        state
                    }
                }
                LexState.WEAKLY_QUOTED_WORD -> when (it) {
                    '\"' -> {
                        if (rawToken.isEmpty()) result.add(WORD(""))
                        LexState.UNQUOTED_WORD
                    }
                    else -> {
                        rawToken.append(it)
                        state
                    }
                }
            }
        }
        return when (state) {
            LexState.UNQUOTED_WORD -> {
                if (rawToken.isNotEmpty()) result.add(WORD(rawToken.toString()))
                result
            }
            LexState.WHITESPACE -> {
                result
            }
            LexState.FULLY_QUOTED_WORD, LexState.WEAKLY_QUOTED_WORD -> {
                throw UnterminatedQuotesException(position)
            }
        }
    }

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
