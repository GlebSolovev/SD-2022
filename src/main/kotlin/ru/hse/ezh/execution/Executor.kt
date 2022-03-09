package ru.hse.ezh.execution

import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.CommandStartupException
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.commands.ExitCommand

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * This class is responsible for running a list of operations sequentially as a pipe.
 *
 * [Executor] is also responsible for determining a specific action depending on [Operation] type:
 * - [Assignment.doAssign] for [Assignment]
 * - [Command.execute] for [Command]
 * - exiting for [ExitCommand]
 */
object Executor {

    /**
     * Performs [operations] sequentially, passing:
     * - output stream as input stream (pipe)
     * - status codes
     * - local environment (copy of [globalEnv] that can be modified during execution of [operations])
     * from one operation to the next one.
     *
     * If [operations] contains a single [Assignment], it will be performed on [globalEnv] rather than on its copy.
     *
     * If an [ExitCommand] is encountered, the [Environment.exitStatus] in [globalEnv] is set to
     * [Environment.ExitStatus.EXITING] and the rest of [operations] are not executed.
     *
     * If [operations] is empty, a zero status code and empty streams are returned.
     *
     * @param operations The list of operations to execute. Must be not empty.
     * @param globalEnv The global session environment.
     *
     * @return Last command exit code, output stream and error stream.
     *
     * @throws CommandStartupException See [Command.execute].
     * @throws ExecutionIOException See [Command.execute].
     */
    @Throws(CommandStartupException::class, ExecutionIOException::class)
    fun execute(operations: List<Operation>, globalEnv: Environment): Triple<Int, InputStream, InputStream> {
        val emptyInputStream = InputStream.nullInputStream()
        if (operations.isEmpty()) return Triple(0, emptyInputStream, emptyInputStream)

        val localEnv = Environment().replaceWith(globalEnv)
        var input = emptyInputStream
        var out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        var exitCode = 0

        loop@ for (op in operations) {
            when (op) {
                is Assignment -> {
                    op.doAssign(localEnv)
                }
                is Command -> {
                    out = ByteArrayOutputStream()
                    exitCode = op.execute(input, out, err, localEnv)
                    input = ByteArrayInputStream(out.toByteArray())
                    if (localEnv.exitStatus == Environment.ExitStatus.EXITING) break@loop
                }
            }
        }

        if (operations.size == 1) globalEnv.replaceWith(localEnv)

        return Triple(
            exitCode,
            ByteArrayInputStream(out.toByteArray()),
            ByteArrayInputStream(err.toByteArray())
        )
    }

}
