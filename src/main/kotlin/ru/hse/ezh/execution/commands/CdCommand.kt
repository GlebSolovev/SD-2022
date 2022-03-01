package ru.hse.ezh.execution.commands

import javax.swing.filechooser.FileSystemView
import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.commands.utils.writeLineWrapped
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * This class represents the 'cd' command.
 *
 * Changes current working directory.
 *
 * @constructor
 * @param args Command arguments: contains target directory, otherwise change working directory to the root.
 */
class CdCommand(args: List<String>) : Command(args) {
    /**
     * Executes the command (see [Command.execute]).
     *
     * Prints target directory content or file name if file name is provided to the [out].
     *
     * @param input Ignored.
     * @param out Ignored.
     * @param err Prints error if failed.
     * @param env Changes working directory.
     *
     * @return
     * - 0 on success
     * - 1 if directory does not exist
     * - 2 if target is not a directory
     *
     * @throws ExecutionIOException If [out] stream error occurred.
     */
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        var dir = File.listRoots()[0]

        if (args.isNotEmpty()) {
            if (args[0].startsWith(System.getProperty("user.dir")))
                dir = File(args[0])
            else
                dir = File(System.getProperty("user.dir"), args[0])
        }

        if (!dir.exists()) {
            err.writeLineWrapped("cd: File or directory ${dir} does not exist")
            return 1
        }
        if (!dir.isDirectory) {
            err.writeLineWrapped("cd: ${dir} is not a directory")
            return 2
        }
        env.putVariable("user.dir", dir.canonicalPath)
        System.setProperty("user.dir", dir.canonicalPath)
        return 0
    }
}
