package ru.hse.ezh.exceptions

import ru.hse.ezh.parsing.PIPE
import ru.hse.ezh.parsing.Parser
import ru.hse.ezh.parsing.Token

/**
 * This class represents [Parser] exceptions.
 *
 * Generated [Exception] message contains both detailed error description and last successfully parsed token.
 *
 * @constructor
 * @param message Detail message.
 * @param lastToken Last token that was successfully parsed before an error.
 * Is null if such doesn't exist or is unknown.
 */
sealed class ParserException(message: String, lastToken: Token?) : Exception("$message, last valid token: $lastToken")

/**
 * This exception is thrown when LHS of assignment is empty.
 *
 * Generated [ParserException] message contains predefined exception description.
 *
 * @constructor
 * @param lastToken See [ParserException] constructor.
 */
class EmptyLHSException(lastToken: Token?) : ParserException("empty LHS of assignment", lastToken)

/**
 * This exception is thrown when RHS of assignment is empty.
 *
 * Generated [ParserException] message contains predefined exception description.
 *
 * @constructor
 * @param lastToken See [ParserException] constructor.
 */
class EmptyRHSException(lastToken: Token?) : ParserException("empty RHS of assignment", lastToken)

/**
 * This exception is thrown when consecutive operations are not separated by [PIPE].
 *
 * Generated [ParserException] message contains predefined exception description.
 *
 * @constructor
 * @param lastToken See [ParserException] constructor.
 */
class NotPipedOperationsException(lastToken: Token?) : ParserException("sequential operations without pipe", lastToken)
