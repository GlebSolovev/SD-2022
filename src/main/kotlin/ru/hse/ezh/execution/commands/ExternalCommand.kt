package ru.hse.ezh.execution.commands

import org.buildobjects.process.ProcBuilder
import org.buildobjects.process.StartupException
import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.CommandStartupException
import ru.hse.ezh.execution.Command

import java.io.File
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
class ExternalCommand(private val name: String, args: List<String>) : Command(args) {

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
     * @throws CommandStartupException If external command named [name] failed to startup.
     */
    @Throws(CommandStartupException::class)
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        @Suppress("SpreadOperator")
        val procBuilder = ProcBuilder(name, *args.toTypedArray())
            .withVars(env.getAllVariables())
            .withInputStream(input)
            .withOutputStream(out)
            .withErrorStream(err)
            .withNoTimeout()
            .ignoreExitStatus()
        procBuilder.withWorkingDirectory(File(System.getProperty("user.dir")))
        @Suppress("SwallowedException")
        try {
            return procBuilder.run().exitValue
        } catch (e: StartupException) {
            throw CommandStartupException("$name: could not startup process\n" + (e.cause?.message ?: "unknown reason"))
        }
    }
}
