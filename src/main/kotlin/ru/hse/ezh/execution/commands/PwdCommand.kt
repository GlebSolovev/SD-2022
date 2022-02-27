package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.commands.utils.writeLineWrapped
import ru.hse.ezh.execution.commands.utils.writeWrapped

import java.io.InputStream
import java.io.OutputStream

import kotlin.jvm.Throws

/**
 * This class represents the 'pwd' command.
 *
 * Prints the current working directory absolute path.
 *
 * @constructor
 * @param args Command arguments: empty list.
 */
class PwdCommand(args: List<String>) : Command(args) {

    /**
     * Executes the command (see [Command.execute]).
     *
     * Prints the current working directory absolute path to [out].
     *
     * @param input Ignored.
     * @param out Stream to print output to.
     * @param err Stream to print errors to.
     * @param env Ignored.
     *
     * @return
     * - 0 on success
     * - 1 if argument list is invalid
     *
     * @throws ExecutionIOException If [out] stream error occurred.
     */
    @Throws(ExecutionIOException::class)
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        if (args.isNotEmpty()) {
            err.writeLineWrapped("pwd: expected zero arguments")
            return ReturnCode.INVALID_ARGS.code
        }
        out.writeWrapped(System.getProperty("user.dir"))
        return ReturnCode.SUCCESS.code
    }

    private enum class ReturnCode(val code: Int) {
        SUCCESS(0),
        INVALID_ARGS(1)
    }

}
