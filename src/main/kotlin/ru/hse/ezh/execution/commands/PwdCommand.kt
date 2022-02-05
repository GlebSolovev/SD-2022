package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command

import java.io.IOException
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
            try {
                err.write("pwd: expected zero arguments".toByteArray())
            } catch (e: IOException) {
                throw ExecutionIOException("err stream error", e)
            }
            return 1
        }
        try {
            out.write(System.getProperty("user.dir").toByteArray())
        } catch (e: IOException) {
            throw ExecutionIOException("out stream error", e)
        }
        return 0
    }
}
