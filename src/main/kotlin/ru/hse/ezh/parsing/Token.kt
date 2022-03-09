package ru.hse.ezh.parsing

/**
 * Tokens of Ezh shell language.
 */
sealed class Token

/**
 * Represents a single string that should be interpreted as an indivisible element.
 * In particular, a [WORD] can have space characters.
 *
 * For example: a single command argument, command name, variable value.
 *
 * @constructor
 * @param str Underlying string.
 */
data class WORD(val str: String) : Token()

/**
 * Represents a variable name (outside of quotes) that will be substituted with its value.
 *
 * @constructor
 * @param varName Variable name.
 */
data class SUBST(val varName: String) : Token()

/**
 * Represents a variable name inside of quotes that will be substituted with its value.
 *
 * @constructor
 * @param varName Variable name.
 */
data class QSUBST(val varName: String) : Token()

/**
 * Represents one of space characters or a sequence of them. Used as tokens delimiter.
 */
object SPACE : Token() {
    override fun toString() = "SPACE"
}

/**
 * Represents an assignment symbol as an operation.
 */
object ASSIGN : Token() {
    override fun toString() = "ASSIGN"
}

/**
 * Represents a pipe symbol.
 */
object PIPE : Token() {
    override fun toString() = "PIPE"
}
