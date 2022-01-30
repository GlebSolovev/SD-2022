package ru.hse.ezh

/**
 * Class for storing environment variables and other session context.
 *
 * This same class can be used both as a global and a local (as in pipes) environment.
 */
class Environment {

    enum class ExitStatus { RUNNING, EXITING }

    private val variables: MutableMap<WORD, WORD> = TODO("Not yet implemented")
    var exitStatus: ExitStatus = TODO("Not yet implemented")
        set(exitStatus) = TODO("Not yet implemented")

    fun putVariable(name: WORD, value: WORD): Unit = TODO("Not yet implemented")

    fun getVariable(name: WORD): WORD = TODO("Not yet implemented")

}
