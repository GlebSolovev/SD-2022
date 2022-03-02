package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.commands.utils.CHARSET
import ru.hse.ezh.execution.commands.utils.readAllWrapped
import ru.hse.ezh.execution.commands.utils.writeLineWrapped
import ru.hse.ezh.execution.commands.utils.writeWrapped

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

import kotlin.jvm.Throws

/**
 * This class represents the 'cat' command.
 *
 * Prints file contents to output.
 *
 * @constructor
 * @param args Command arguments: list of either 1 or 0 elements.
 *  - [args]`[0]` - filename, optional
 */
class CatCommand(args: List<String>) : Command(args) {

    /**
     * Executes the command (see [Command.execute]).
     *
     * If a filename is given, prints its contents in UTF-8 to [out].
     * Else prints [input] to [out].
     *
     * Does not support huge files (> 2 GB).
     *
     * @param input Stream to read input from in case no filename is given.
     * @param out Stream to print output to.
     * @param err Stream to print errors to.
     * @param env Keeps current working directory.
     *
     * @return
     * - 0 on success
     * - 1 if argument list is invalid
     * - 2 if an [java.io.IOException] happens during reading file
     *
     * @throws ExecutionIOException If [input] or [out] stream error occurred.
     */
    @Throws(ExecutionIOException::class)
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        if (args.size > 1) {
            err.writeLineWrapped("cat: expected one or zero arguments")
            return 1
        }
        val content = if (args.size == 1) {
            try {
                if (args[0].startsWith(env.workingDirectory))
                    File(args[0]).readText(CHARSET)
                else
                    File(env.workingDirectory, args[0]).readText(CHARSET)
            } catch (e: IOException) {
                err.writeLineWrapped("cat: IOException during reading file\n${e.message}")
                return 2
            }
        } else {
            input.readAllWrapped()
        }
        out.writeWrapped(content)
        return 0
    }
}
