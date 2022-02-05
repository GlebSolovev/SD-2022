package ru.hse.ezh.views

import ru.hse.ezh.exceptions.ViewException

import java.io.InputStream

import kotlin.jvm.Throws

/**
 * This interface is responsible for interaction with user.
 */
interface View {

    /**
     * Gets a single instruction from user as text.
     *
     * An instruction is either an operation or a sequence of piped operations.
     *
     * @return A single user instruction as text. Must be finite.
     *
     * @throws ViewException If an input error occurred.
     */
    @Throws(ViewException::class)
    fun getInput(): Sequence<Char>

    /**
     * Shows user the results of an instruction written to [out] stream.
     *
     * @param out A stream containing the results of an instruction.
     *
     * @throws ViewException If an output error occurred.
     */
    @Throws(ViewException::class)
    fun writeOutput(out: InputStream)

    /**
     * Shows user the errors that happened during instruction execution.
     *
     * @param err A stream containing the errors of an instruction.
     *
     * @throws ViewException If an output error occurred.
     */
    @Throws(ViewException::class)
    fun writeError(err: InputStream)

}
