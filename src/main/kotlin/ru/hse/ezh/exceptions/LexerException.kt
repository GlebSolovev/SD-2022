package ru.hse.ezh.exceptions

import ru.hse.ezh.parsing.ASSIGN
import ru.hse.ezh.parsing.Lexer
import ru.hse.ezh.parsing.SPACE

/**
 * This class represents [Lexer] exceptions.
 *
 * Generated [Exception] message contains both detailed error description and position of the error in the input text.
 *
 * @constructor
 * @param message Detail message.
 * @param position Position in the input text where the error occurred. Is null if such is unknown.
 */
sealed class LexerException(message: String, position: Int?) :
    Exception("$message, at position: ${position ?: "unknown"}")

/**
 * This exception is thrown when an unterminated quote is encountered.
 *
 * Generated [LexerException] message contains predefined exception description.
 *
 * @constructor
 * @param position See [LexerException] constructor.
 */
class UnterminatedQuotesException(position: Int) : LexerException("unterminated quotes", position)

/**
 * This exception is thrown when an empty variable name in a substitution is encountered.
 *
 * Generated [LexerException] message contains predefined exception description.
 */
class EmptySubstitutionException(position: Int) :
    LexerException("empty substitution is forbidden: no variable name", position)

/**
 * This exception is thrown when [SPACE] near [ASSIGN] is encountered.
 *
 * Generated [LexerException] message contains predefined exception description, but the error position is unknown.
 */
class SpaceNearAssignException : LexerException("space near assign is forbidden", null)
