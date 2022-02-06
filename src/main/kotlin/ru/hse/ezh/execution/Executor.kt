package ru.hse.ezh.execution

import ru.hse.ezh.Environment
import ru.hse.ezh.execution.commands.utils.convertToInput

import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * This class is responsible for running a list of operations sequentially as a pipe.
 *
 * [Executor] is also responsible for determining a specific action depending on [Operation] type:
 * - [Assignment.doAssign] for [Assignment]
 * - [Command.execute] for [Command]
 * - exiting for [Exit]
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
     * If an [Exit] is encountered, the [Environment.exitStatus] in [globalEnv] is set to
     * [Environment.ExitStatus.EXITING] and the rest of [operations] are not executed.
     *
     * @param operations The list of operations to execute. Must be not empty.
     * @param globalEnv The global session environment.
     *
     * @return Last command exit code, output stream and error stream.
     */
    fun execute(operations: List<Operation>, globalEnv: Environment): Triple<Int, InputStream, InputStream> {
        if (operations.isEmpty()) throw IllegalArgumentException("nothing to execute")
        if (operations.size > 1) {
            TODO("pipe")
        }
        val emptyInputStream = InputStream.nullInputStream()
        when (val op = operations[0]) {
            is Assignment -> {
                op.doAssign(globalEnv)
                return Triple(0, emptyInputStream, emptyInputStream)
            }
            is Command -> {
                val out = ByteArrayOutputStream()
                val err = ByteArrayOutputStream()
                val exitCode = op.execute(emptyInputStream, out, err, globalEnv)
                return Triple(exitCode, out.convertToInput(), err.convertToInput())
            }
            is Exit -> {
                globalEnv.exitStatus = Environment.ExitStatus.EXITING
                return Triple(op.statusCode, emptyInputStream, emptyInputStream)
            }
        }
    }

}
