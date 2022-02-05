package ru.hse.ezh

import ru.hse.ezh.parsing.WORD

/**
 * Class for storing environment variables and other session context.
 *
 * This same class can be used both as a global and a local (as in pipes) environment.
 */
class Environment {

    /**
     * Represents the status of current session. Used to terminate the session.
     */
    enum class ExitStatus { RUNNING, EXITING }

    private val variables: MutableMap<WORD, WORD> = TODO("Not yet implemented")

    /**
     * Status of current session.
     */
    var exitStatus: ExitStatus = TODO("Not yet implemented")
        /**
         * @throws IllegalStateException If the [exitStatus] is set to [ExitStatus.EXITING].
         */
        set(exitStatus) = TODO("Not yet implemented")

    /**
     * Puts a variable into this environment.
     *
     * If the variable already exists, its value is overwritten.
     * Else the variable is created with the value.
     *
     * @param name Variable name.
     * @param value Variable new value.
     *
     * @throws IllegalStateException If the [exitStatus] is set to [ExitStatus.EXITING].
     */
    fun putVariable(name: WORD, value: WORD): Unit = TODO("Not yet implemented")

    /**
     * Gets the value of a variable named [name] from this environment.
     *
     * If no such variable exists, a [WORD] containing an empty string is returned.
     *
     * @param name Variable name.
     *
     * @return Variable value or an empty [WORD].
     *
     * @throws IllegalStateException If the [exitStatus] is set to [ExitStatus.EXITING].
     */
    fun getVariable(name: WORD): WORD = TODO("Not yet implemented")

}
