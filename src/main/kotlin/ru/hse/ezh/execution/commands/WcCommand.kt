package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.commands.utils.CHARSET
import ru.hse.ezh.execution.commands.utils.readAllBytesWrapped
import ru.hse.ezh.execution.commands.utils.writeLineWrapped
import ru.hse.ezh.execution.commands.utils.writeWrapped

import java.io.*

import kotlin.jvm.Throws

/**
 * This class represents the 'wc' command.
 *
 * Prints number of lines, words and bytes in a file to output.
 *
 * @constructor
 * @param args Command arguments: list of either 1 or 0 elements.
 *  - [args]`[0]` - filename, optional
 */
class WcCommand(args: List<String>) : Command(args) {

    /**
     * Executes the command (see [Command.execute]).
     *
     * If a filename is given, reads its contents as input. Else reads from [input] instead of file.
     *
     * Counts the lines (uses system line endings), words (delimited by \s) and bytes in input.
     * Then prints the counted numbers to [out] delimited by \t.
     *
     * Does not support huge files (> 2 GB).
     *
     * @param input Stream to read input from in case no filename is given.
     * @param out Stream to print output to.
     * @param err Stream to print errors to.
     * @param env Ignored.
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
            err.writeLineWrapped("wc: expected one or zero arguments")
            return 1
        }

        val content = if (args.size == 1) {
            try {
                File(args[0]).readBytes()
            } catch (e: IOException) {
                err.writeLineWrapped("wc: IOException during reading file\n${e.message}")
                return 2
            }
        } else {
            input.readAllBytesWrapped()
        }

        var lines = 0
        var words = 0
        val bytes = content.size

        fun handleLine(line: String) {
            lines += 1
            words += line.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        }
        String(content, CHARSET).lines().forEach { handleLine(it) }

        out.writeWrapped("$lines\t$words\t$bytes")
        return 0
    }
}
