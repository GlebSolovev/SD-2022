package ru.hse.ezh.exceptions

import ru.hse.ezh.views.View

/**
 * This class represents [View] exceptions.
 *
 * Its detailed error description and [Throwable] cause are passed directly to the corresponding [Exception] parameters.
 *
 * @constructor
 * @param message Detail message.
 * @param cause [Throwable] cause of the exception if it exists.
 */
class ViewException(message: String, cause: Throwable? = null) : Exception(message, cause)
