package ru.hse.ezh

import java.io.InputStream

interface View {

    fun getInput(): Sequence<Char>

    fun writeOutput(out: InputStream)

    fun writeError(err: InputStream)

}
