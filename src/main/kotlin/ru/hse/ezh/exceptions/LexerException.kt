package ru.hse.ezh.exceptions

sealed class LexerException(message: String, position: Int?) :
    Exception("$message, at position: ${position ?: "unknown"}")

class UnterminatedQuotesException(position: Int) : LexerException("unterminated quotes", position)

class SpaceNearAssignException : LexerException("space near assign is forbidden", null)
