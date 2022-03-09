package ru.hse.ezh.execution.commands.utils

import ru.hse.ezh.exceptions.ExecutionIOException

import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

import kotlin.jvm.Throws

val CHARSET: Charset = StandardCharsets.UTF_8

@Throws(ExecutionIOException::class)
internal fun InputStream.readAllBytesWrapped(errorMessage: String = "internal input stream error"): ByteArray {
    try {
        return this.readAllBytes()
    } catch (_: IOException) {
        throw ExecutionIOException(errorMessage)
    }
}

@Throws(ExecutionIOException::class)
internal fun InputStream.readAllWrapped(errorMessage: String = "internal input stream error") =
    String(readAllBytesWrapped(errorMessage), CHARSET)

@Throws(ExecutionIOException::class)
internal fun OutputStream.writeWrapped(str: String, errorMessage: String = "internal output stream error") {
    try {
        this.write(str.toByteArray(CHARSET))
    } catch (_: IOException) {
        throw ExecutionIOException(errorMessage)
    }
}

@Throws(ExecutionIOException::class)
internal fun OutputStream.writeLineWrapped(str: String, errorMessage: String = "internal output stream error") =
    writeWrapped(str + "\n", errorMessage)
