package ru.hse.ezh

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

    private val variables: MutableMap<String, String> = mutableMapOf()

    /**
     * Status of current session.
     */
    var exitStatus: ExitStatus = ExitStatus.RUNNING
        /**
         * @throws IllegalStateException If the [exitStatus] is set to [ExitStatus.EXITING].
         */
        set(exitStatus) {
            if (field == ExitStatus.EXITING) {
                throw IllegalStateException("resetting EXITING status is forbidden")
            }
            field = exitStatus
        }

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
    fun putVariable(name: String, value: String) {
        if (exitStatus == ExitStatus.EXITING) {
            throw IllegalStateException("putVariable in EXITING status is forbidden")
        }
        variables[name] = value
    }

    /**
     * Gets the value of a variable named [name] from this environment.
     *
     * If no such variable exists, an empty string is returned.
     *
     * @param name Variable name.
     *
     * @return Variable value or an empty string.
     *
     * @throws IllegalStateException If the [exitStatus] is set to [ExitStatus.EXITING].
     */
    fun getVariable(name: String): String {
        if (exitStatus == ExitStatus.EXITING) {
            throw IllegalStateException("getVariable in EXITING status is forbidden")
        }
        return variables[name] ?: ""
    }

}
