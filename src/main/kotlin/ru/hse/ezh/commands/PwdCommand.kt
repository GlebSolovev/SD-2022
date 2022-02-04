package ru.hse.ezh.commands

import ru.hse.ezh.Command
import ru.hse.ezh.Environment
import java.io.InputStream
import java.io.OutputStream

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
     */
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        TODO("Not yet implemented")
    }
}
