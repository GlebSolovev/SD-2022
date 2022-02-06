package ru.hse.ezh.exceptions

sealed class LexerException(message: String, position: Int) : Exception("$message, at position: $position")

class UnterminatedQuotesException(position: Int) : LexerException("unterminated quotes", position)
