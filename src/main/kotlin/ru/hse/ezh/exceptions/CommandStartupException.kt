package ru.hse.ezh.exceptions

import ru.hse.ezh.execution.commands.ExternalCommand

/**
 * This exception is thrown from [ExternalCommand.execute] when the corresponding external command failed to startup.
 *
 * Its detailed error description is passed directly to the corresponding [Exception] parameter.
 *
 * @constructor
 * @param message Detail message.
 */
class CommandStartupException(message: String) : Exception(message)
