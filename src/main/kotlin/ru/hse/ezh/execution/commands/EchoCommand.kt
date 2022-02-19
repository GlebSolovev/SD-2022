package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.commands.utils.writeWrapped

import java.io.InputStream
import java.io.OutputStream

import kotlin.jvm.Throws

/**
 * This class represents the 'echo' command.
 *
 * Prints its arguments to output.
 *
 * @constructor
 * @param args Command arguments: list of any number of strings.
 */
class EchoCommand(args: List<String>) : Command(args) {

    /**
     * Executes the command (see [Command.execute]).
     *
     * Prints arguments as UTF-8 text, joined with whitespace, to [out].
     *
     * @param input Ignored.
     * @param out Stream to print output to.
     * @param err Ignored.
     * @param env Ignored.
     *
     * @return
     * - 0 always
     *
     * @throws ExecutionIOException If [out] stream error occurred.
     */
    @Throws(ExecutionIOException::class)
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        out.writeWrapped(args.joinToString(separator = " ", postfix = "\n"))
        return 0
    }

}
