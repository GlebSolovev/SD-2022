package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.commands.utils.writeLineWrapped

import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * This class represents the 'ls' command.
 *
 * Prints a list of files in the target directory.
 *
 * @constructor
 * @param args Command arguments: contains target directory or file, otherwise current directory is used.
 */
class LsCommand(args: List<String>) : Command(args) {
    /**
     * Executes the command (see [Command.execute]).
     *
     * Prints target directory content or file name if file name is provided to the [out].
     *
     * @param input Ignored.
     * @param out Stream to print output to.
     * @param err Prints error if file does not exist.
     * @param env Keeps current working directory.
     *
     * @return
     * - 0 on success
     * - 1 if file does not exist
     *
     * @throws ExecutionIOException If [out] stream error occurred.
     */
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        var target = env.workingDirectory + File.separator + "."
        if (args.isNotEmpty()) {
            target = if (args[0].startsWith(env.workingDirectory))
                args[0]
            else
                env.workingDirectory + File.separator + args[0]
        }

        val dir = File(target)

        if (!dir.exists()) {
            err.writeLineWrapped("ls: File or directory $dir does not exist")
            return 1
        }
        if (!dir.isDirectory) {
            out.writeLineWrapped(target)
            return 0
        }
        dir.walk().maxDepth(1)
            .filter { it.canonicalPath != dir.canonicalPath }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            .forEach { out.writeLineWrapped(it.name) }
        return 0
    }
}
