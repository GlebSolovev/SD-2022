package ru.hse.ezh.execution

import ru.hse.ezh.Environment
import ru.hse.ezh.parsing.WORD

import java.io.InputStream
import java.io.OutputStream

/**
 * This class represents Ezh operations: assignment, calling a command, exiting.
 */
sealed class Operation

/**
 * This class represents an assignment operation.
 *
 * Assigns a new value to an environment variable.
 *
 * @constructor
 * @param lhs Variable name.
 * @param rhs Variable new value.
 */
class Assignment(private val lhs: WORD, private val rhs: WORD) : Operation() {

    /**
     * Performs assignment in [env] environment using [Environment.putVariable].
     *
     * @param env The environment to modify.
     */
    fun doAssign(env: Environment) {
        env.putVariable(lhs.str, rhs.str)
    }

}

/**
 * Abstract superclass for external and Ezh commands.
 *
 * @constructor
 * @param args Arguments for the command.
 */
abstract class Command(protected val args: List<String>) : Operation() {

    /**
     * Executes the command.
     *
     * The command must interpret [input] as stdin, [out] as stdout and [err] as stderr.
     * It can have access to [env] information like variables.
     *
     * The return value must be command's exit code.
     */
    abstract fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int

}
