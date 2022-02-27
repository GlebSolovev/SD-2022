package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.commands.utils.writeLineWrapped

import java.io.InputStream
import java.io.OutputStream

import kotlin.jvm.Throws

/**
 * This class represents the 'exit' command.
 *
 * Sets the given [Environment.exitStatus] to [Environment.ExitStatus.EXITING].
 *
 * @constructor
 * @param args Command arguments: list of either 1 or 0 elements.
 *  - [args]`[0]` - status code, optional. Must parse into an integer.
 */
class ExitCommand(args: List<String>) : Command(args) {

    /**
     * Executes the command (see [Command.execute]).
     *
     * Sets the [Environment.exitStatus] in [env] to [Environment.ExitStatus.EXITING],
     * even if [args] are not valid.
     *
     * No commands should be executed in [env] after this one.
     *
     * If a status code is given, it is used as this command exit code.
     * Else 0 is used.
     *
     * @param input Ignored.
     * @param out Ignored.
     * @param err Stream to print errors to.
     * @param env Environment to modify.
     *
     * @return
     * - given status code or 0 on success
     * - 1 if argument list is invalid
     * - 2 if `args[0]` does not parse into an integer
     *
     * @throws ExecutionIOException If [out] stream error occurred.
     */
    @Throws(ExecutionIOException::class)
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        env.exitStatus = Environment.ExitStatus.EXITING
        return if (args.size == 1) {
            args[0].toIntOrNull() ?: 2.also { err.writeLineWrapped("exit: expected integer status code") }
        } else if (args.size > 1) {
            1.also { err.writeLineWrapped("exit: expected one or zero arguments") }
        } else 0
    }

}
