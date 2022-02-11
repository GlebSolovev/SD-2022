package ru.hse.ezh.execution

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.CommandStartupException
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.commands.ExternalCommand
import ru.hse.ezh.parsing.WORD

import java.io.InputStream
import java.io.OutputStream
import java.util.*

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
data class Assignment(private val lhs: WORD, private val rhs: WORD) : Operation() {

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
     *
     * @throws CommandStartupException If external command of [ExternalCommand] failed to startup.
     * @throws ExecutionIOException If [input], [out] or [err] stream error occurred.
     */
    @Throws(CommandStartupException::class, ExecutionIOException::class)
    abstract fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int

    /**
     * Commands are equal if and only if their types and [args] are equal.
     */
    override fun equals(other: Any?): Boolean {
        return if (other != null && other::class == this::class) args == (other as Command).args else false
    }

    /**
     * Generates hash code according to [equals].
     */
    override fun hashCode(): Int {
        return Objects.hash(this::class, args)
    }

}
