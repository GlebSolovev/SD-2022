package ru.hse.ezh.commands

import ru.hse.ezh.Command
import ru.hse.ezh.Environment
import java.io.InputStream
import java.io.OutputStream

/**
 * This class represents a command that is not an Ezh command.
 *
 * Calls a given external command with given arguments.
 *
 * @constructor
 * @param name Command name.
 * @param args Command arguments.
 */
class ExternalCommand(val name: String, args: List<String>) : Command(args) {

    /**
     * Executes an external command (see [Command.execute]).
     *
     * Calls an external command named [name] with [args] as arguments as a system process.
     *
     * The created process will have [input] as its stdin, [out] as its stdout and [err] as its stderr.
     * The variables from [env] will be added to this process' environment.
     *
     * @param input External command stdin stream.
     * @param out External command stdout stream.
     * @param err External command stderr stream.
     * @param env Additional environment variables.
     *
     * @return External command return code.
     *
     * @throws CommandNotFoundException If no external command named [name] was found.
     */
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        TODO("Not yet implemented")
    }
}
