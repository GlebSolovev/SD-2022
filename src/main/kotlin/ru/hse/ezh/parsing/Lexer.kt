package ru.hse.ezh.parsing

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.EmptySubstitutionException
import ru.hse.ezh.exceptions.SpaceNearAssignException
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
        SUBSTITUTION,
        WEAKLY_QUOTED_SUBSTITUTION
    }

    /**
     * Splits [input] into a list of tokens based on special characters (see the "Lexer" automaton
     * in scheme.pdf in the root directory).
     *
     * @param input A single user instruction as text. Must be finite.
     *
     * @return The resulting tokens.
     *
     * @throws UnterminatedQuotesException If an unterminated quote is encountered.
     * @throws EmptySubstitutionException If an empty variable name in a substitution is encountered.
     */
    @Suppress("LongMethod", "NestedBlockDepth", "ComplexMethod") // is ok for automata
    @Throws(UnterminatedQuotesException::class, EmptySubstitutionException::class)
    fun lex(input: Sequence<Char>): List<Token> {
        val result = mutableListOf<Token>()
        var state = LexState.UNQUOTED_WORD
        val rawToken = StringBuilder()
        var position = 0

        fun addNotEmptyWord() {
            if (rawToken.isNotEmpty()) {
                result.add(WORD(rawToken.toString()))
                rawToken.clear()
            }
        }

        fun addNotEmptySubstitutionOrThrow(isQuoted: Boolean) {
            if (rawToken.isEmpty()) throw EmptySubstitutionException(position)
            result.add((if (isQuoted) ::QSUBST else ::SUBST)(rawToken.toString()))
            rawToken.clear()
        }

        fun addNotEmptySubstOrThrow() = addNotEmptySubstitutionOrThrow(false)

        fun addNotEmptyQSubstOrThrow() = addNotEmptySubstitutionOrThrow(true)

        input.forEach {
            position++
            state = when (state) {
                LexState.UNQUOTED_WORD -> {
                    when (it) {
                        '\'' -> LexState.FULLY_QUOTED_WORD
                        '\"' -> LexState.WEAKLY_QUOTED_WORD
                        '=' -> {
                            addNotEmptyWord()
                            result.add(ASSIGN)
                            state
                        }
                        '|' -> {
                            addNotEmptyWord()
                            result.add(PIPE)
                            state
                        }
                        '$' -> {
                            addNotEmptyWord()
                            LexState.SUBSTITUTION
                        }
                        else -> {
                            if (isWhitespace(it)) {
                                addNotEmptyWord()
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
                    '=' -> {
                        result.add(ASSIGN)
                        LexState.UNQUOTED_WORD
                    }
                    '|' -> {
                        result.add(PIPE)
                        LexState.UNQUOTED_WORD
                    }
                    '$' -> LexState.SUBSTITUTION
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
                    '$' -> {
                        addNotEmptyWord()
                        LexState.WEAKLY_QUOTED_SUBSTITUTION
                    }
                    else -> {
                        rawToken.append(it)
                        state
                    }
                }
                LexState.SUBSTITUTION -> when (it) {
                    '\'' -> {
                        addNotEmptySubstOrThrow()
                        LexState.FULLY_QUOTED_WORD
                    }
                    '\"' -> {
                        addNotEmptySubstOrThrow()
                        LexState.WEAKLY_QUOTED_WORD
                    }
                    '=' -> {
                        addNotEmptySubstOrThrow()
                        result.add(ASSIGN)
                        LexState.UNQUOTED_WORD
                    }
                    '|' -> {
                        addNotEmptySubstOrThrow()
                        result.add(PIPE)
                        LexState.UNQUOTED_WORD
                    }
                    '$' -> {
                        addNotEmptySubstOrThrow()
                        state
                    }
                    else -> {
                        if (isWhitespace(it)) {
                            addNotEmptySubstOrThrow()
                            result.add(SPACE)
                            LexState.WHITESPACE
                        } else {
                            rawToken.append(it)
                            state
                        }
                    }
                }
                LexState.WEAKLY_QUOTED_SUBSTITUTION -> when (it) {
                    '\'', '=', '|' -> {
                        addNotEmptyQSubstOrThrow()
                        rawToken.append(it)
                        LexState.WEAKLY_QUOTED_WORD
                    }
                    '\"' -> {
                        addNotEmptyQSubstOrThrow()
                        LexState.UNQUOTED_WORD
                    }
                    '$' -> {
                        addNotEmptyQSubstOrThrow()
                        state
                    }
                    else -> {
                        if (isWhitespace(it)) {
                            addNotEmptyQSubstOrThrow()
                            rawToken.append(it)
                            LexState.WEAKLY_QUOTED_WORD
                        } else {
                            rawToken.append(it)
                            state
                        }
                    }
                }
            }
        }
        return when (state) {
            LexState.UNQUOTED_WORD -> {
                addNotEmptyWord()
                result
            }
            LexState.WHITESPACE -> {
                result
            }
            LexState.FULLY_QUOTED_WORD, LexState.WEAKLY_QUOTED_WORD, LexState.WEAKLY_QUOTED_SUBSTITUTION -> {
                throw UnterminatedQuotesException(position)
            }
            LexState.SUBSTITUTION -> {
                addNotEmptySubstOrThrow()
                result
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
     *
     * @throws SpaceNearAssignException If [SPACE] near [ASSIGN] is encountered.
     */
    @Throws(SpaceNearAssignException::class)
    fun postprocess(tokens: List<Token>, globalEnv: Environment): List<Token> {
        val result = mutableListOf<Token>()
        val rawWord = StringBuilder()
        var merging = false
        var lastToken: Token? = null

        fun addMerged() {
            if (merging) {
                result.add(WORD(rawWord.toString()))
                rawWord.clear()
            }
        }

        tokens.forEach {
            when (it) {
                is WORD -> {
                    rawWord.append(it.str)
                    merging = true
                }
                is SPACE -> {
                    if (lastToken is ASSIGN) throw SpaceNearAssignException()
                    addMerged()
                    merging = false
                }
                is ASSIGN -> {
                    if (lastToken is SPACE) throw SpaceNearAssignException()
                    addMerged()
                    result.add(ASSIGN)
                    merging = false
                }
                else -> TODO("pipe, subst, qsubst, $globalEnv")
            }
            lastToken = it
        }
        addMerged()

        return result
    }

}
