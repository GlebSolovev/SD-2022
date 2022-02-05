package ru.hse.ezh.execution.commands.utils

import ru.hse.ezh.exceptions.ExecutionIOException

import java.io.IOException
import java.io.OutputStream

import kotlin.jvm.Throws

@Throws(ExecutionIOException::class)
fun OutputStream.writeWrapped(str: String, errorMessage: String = "internal output stream error") {
    try {
        this.write(str.toByteArray())
    } catch (e: IOException) {
        throw ExecutionIOException(errorMessage)
    }
}
