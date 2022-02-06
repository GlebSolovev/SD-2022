package ru.hse.ezh.execution.commands

import org.junit.jupiter.api.Test
import ru.hse.ezh.Environment

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

import kotlin.test.assertEquals

class PwdCommandTest {

    private val input: InputStream = InputStream.nullInputStream()
    private val env = Environment()

    @Test
    fun testSimple() {
        val pwd = PwdCommand(listOf())
        val out = ByteArrayOutputStream()
        val err = OutputStream.nullOutputStream()

        assertEquals(0, pwd.execute(input, out, err, env))
        assertEquals(System.getProperty("user.dir") + "\n", out.toString())
    }

    @Test
    fun testInvalidArguments() {
        val pwd = PwdCommand(listOf("something"))
        val out = OutputStream.nullOutputStream()
        val err = ByteArrayOutputStream()

        assertEquals(1, pwd.execute(input, out, err, env))
        assertEquals("pwd: expected zero arguments\n", err.toString())
    }

}
