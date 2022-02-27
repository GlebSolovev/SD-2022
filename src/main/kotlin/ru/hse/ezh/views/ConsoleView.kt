package ru.hse.ezh.views

import ru.hse.ezh.exceptions.ViewException
import ru.hse.ezh.execution.commands.utils.CHARSET

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

import kotlin.jvm.Throws

/**
 * This class is a CLI [View].
 */
class ConsoleView : View {

    private enum class ReadState { NORMAL, FULLY_QUOTED, WEAKLY_QUOTED }

    /**
     * Reads [System. in] as UTF-8 text until an unquoted \n is encountered.
     *
     * @return A single user instruction as finite text.
     *
     * @throws ViewException If an IOException has occurred.
     */
    @Throws(ViewException::class)
    override fun getInput(): Sequence<Char> {
        val result: MutableList<Char> = mutableListOf()
        val reader = InputStreamReader(System.`in`, CHARSET)

        var state = ReadState.NORMAL
        loop@ while (true) {
            try {
                val c = reader.read()
                if (c == -1) throw ViewException("eof")
                state = when (c.toChar()) {
                    '\n' -> if (state == ReadState.NORMAL) break@loop else state
                    '\'' -> when (state) {
                        ReadState.NORMAL -> ReadState.FULLY_QUOTED
                        ReadState.FULLY_QUOTED -> ReadState.NORMAL
                        else -> state
                    }
                    '"' -> when (state) {
                        ReadState.NORMAL -> ReadState.WEAKLY_QUOTED
                        ReadState.WEAKLY_QUOTED -> ReadState.NORMAL
                        else -> state
                    }
                    else -> state
                }
                result.add(c.toChar())
            } catch (e: IOException) {
                throw ViewException("an IOException has occurred", e)
            }
        }
        return result.asSequence()
    }

    /**
     * Prints [out] to [System.out] with \n postfix.
     *
     * @param out A stream containing the results of an instruction.
     *
     * @throws ViewException If an IOException has occurred.
     */
    @Throws(ViewException::class)
    override fun writeOutput(out: InputStream) {
        try {
            out.transferTo(System.out)
            println()
        } catch (e: IOException) {
            throw ViewException("an IOException has occurred", e)
        }
    }

    /**
     * Prints [err] to [System.err].
     *
     * @param err A stream containing the errors of an instruction.
     *
     * @throws ViewException If an IOException has occurred.
     */
    @Throws(ViewException::class)
    override fun writeError(err: InputStream) {
        try {
            err.transferTo(System.err)
        } catch (e: IOException) {
            throw ViewException("an IOException has occurred", e)
        }
    }

}
