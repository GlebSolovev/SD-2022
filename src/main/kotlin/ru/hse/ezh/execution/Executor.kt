package ru.hse.ezh.execution

import ru.hse.ezh.Environment

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
     * @param operations The list of operations to execute.
     * @param globalEnv The global session environment.
     *
     * @return Last command exit code, output stream and error stream.
     */
    fun execute(operations: List<Operation>, globalEnv: Environment): Triple<Int, InputStream, InputStream> =
        TODO("Not yet implemented")

}
