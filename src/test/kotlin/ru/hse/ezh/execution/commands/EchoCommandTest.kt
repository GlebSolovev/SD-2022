package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

import kotlin.test.Test
import kotlin.test.assertEquals

class EchoCommandTest {

    private val input: InputStream = InputStream.nullInputStream()
    private val err: OutputStream = OutputStream.nullOutputStream()
    private val env = Environment()

    @Test
    fun testSingleWord() {
        val echo = EchoCommand(listOf("word"))
        val out = ByteArrayOutputStream()
        echo.execute(input, out, err, env)

        assertEquals("word\n", out.toString())
    }

    @Test
    fun testEmpty() {
        val echo = EchoCommand(listOf())
        val out = ByteArrayOutputStream()
        echo.execute(input, out, err, env)

        assertEquals("\n", out.toString())
    }

    @Test
    fun testMultipleWords() {
        val echo = EchoCommand(listOf("i", "hate", "kotlin"))
        val out = ByteArrayOutputStream()
        echo.execute(input, out, err, env)

        assertEquals("i hate kotlin\n", out.toString())
    }

    @Test
    fun testSpecialCharacters() {
        val echo = EchoCommand(listOf("", "\t\r\n", "", "", "    ", "ё", "\u9637\""))
        val out = ByteArrayOutputStream()
        echo.execute(input, out, err, env)

        assertEquals(" \t\r\n        ё \u9637\"\n", out.toString())
    }

}
