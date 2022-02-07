package ru.hse.ezh.exceptions

import ru.hse.ezh.parsing.Token

sealed class ParserException(message: String, lastToken: Token?) : Exception("$message, last valid token: $lastToken")

class EmptyLHSException(lastToken: Token?) : ParserException("empty LHS of assignment", lastToken)

class EmptyRHSException(lastToken: Token?) : ParserException("empty RHS of assignment", lastToken)

class NotPipedOperationsException(lastToken: Token?) : ParserException("sequential operations without pipe", lastToken)
