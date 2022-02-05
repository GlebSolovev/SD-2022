package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.execution.Command

import java.io.InputStream
import java.io.OutputStream

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
     * If a filename is given, prints its contents to [out].
     * Else prints [input] to [out].
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
