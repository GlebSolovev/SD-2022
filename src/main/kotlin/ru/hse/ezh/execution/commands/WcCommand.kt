package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.execution.Command

import java.io.InputStream
import java.io.OutputStream

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
     * Counts the lines (use system line endings), words (delimited by \s) and bytes in input.
     * Then prints the counted numbers to [out] delimited by \t.
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
     */
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        TODO("Not yet implemented")
    }
}
