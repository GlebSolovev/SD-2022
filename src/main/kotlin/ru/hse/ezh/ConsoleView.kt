package ru.hse.ezh

import java.io.DataInputStream
import java.io.InputStream

class ConsoleView : View {

    private enum class ReadState { NORMAL, FULLY_QUOTED, WEAKLY_QUOTED }

    override fun getInput(): Sequence<Char> {
        val result: MutableList<Char> = mutableListOf()
        val input = DataInputStream(System.`in`)

        var state = ReadState.NORMAL
        loop@ while (true) {
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
        }
        return result.asSequence()
    }

    override fun writeOutput(out: InputStream) {
        out.transferTo(System.out)
    }

    override fun writeError(err: InputStream) {
        err.transferTo(System.err)
    }

}
