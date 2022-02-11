package ru.hse.ezh.exceptions

import ru.hse.ezh.execution.Command

/**
 * This exception is thrown from [Command.execute] when input, out or err stream error occurred.
 *
 * Its detailed error description and [Throwable] cause are passed directly to the corresponding [Exception] parameters.
 *
 * @constructor
 * @param message Detail message.
 * @param cause [Throwable] cause of the exception if it exists.
 */
class ExecutionIOException(message: String, cause: Throwable? = null) : Exception(message, cause)
