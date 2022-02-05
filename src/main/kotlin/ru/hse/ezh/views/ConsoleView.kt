package ru.hse.ezh.views

import ru.hse.ezh.exceptions.ViewException
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

/**
 * This class is a CLI [View].
 */
class ConsoleView : View {

    private enum class ReadState { NORMAL, FULLY_QUOTED, WEAKLY_QUOTED }

    /**
     * Reads [System. in] until an unquoted \n is encountered.
     *
     * @return A single user instruction as text.
     *
     * @throws ViewException If an IOException has occurred.
     */
    @Throws(ViewException::class)
    override fun getInput(): Sequence<Char> {
        val result: MutableList<Char> = mutableListOf()
        val input = DataInputStream(System.`in`)

        var state = ReadState.NORMAL
        loop@ while (true) {
            try {
                val c = input.readChar()
                state = when (c) {
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
                result.add(c)
            } catch (e: IOException) {
                throw ViewException("an IOException has occurred", e)
            }
        }
        return result.asSequence()
    }

    /**
     * Prints [out] to [System.out].
     *
     * @throws ViewException If an IOException has occurred.
     */
    @Throws(ViewException::class)
    override fun writeOutput(out: InputStream) {
        try {
            out.transferTo(System.out)
        } catch (e: IOException) {
            throw ViewException("an IOException has occurred", e)
        }
    }

    /**
     * Prints [err] to [System.err].
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
