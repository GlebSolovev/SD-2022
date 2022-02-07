package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

import kotlin.test.Test
import kotlin.test.assertEquals

class ExitCommandTest {

    val input = InputStream.nullInputStream()
    val out = OutputStream.nullOutputStream()

    @Test
    fun testExitWithoutStatusCode() {
        val exit = ExitCommand(listOf())
        val err = ByteArrayOutputStream()
        val env = Environment()

        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
        assertEquals(0, exit.execute(input, out, err, env))
        assertEquals(Environment.ExitStatus.EXITING, env.exitStatus)
    }

    @Test
    fun testExitWithStatusCode() {
        val exit = ExitCommand(listOf("5"))
        val err = ByteArrayOutputStream()
        val env = Environment()

        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
        assertEquals(5, exit.execute(input, out, err, env))
        assertEquals(Environment.ExitStatus.EXITING, env.exitStatus)
    }

    @Test
    fun testExitWithInvalidArguments() {
        val exit = ExitCommand(listOf("5", "5"))
        val err = ByteArrayOutputStream()
        val env = Environment()

        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
        assertEquals(1, exit.execute(input, out, err, env))
        assertEquals(Environment.ExitStatus.EXITING, env.exitStatus)
    }

    @Test
    fun testExitWithNonIntegerArgument() {
        val exit = ExitCommand(listOf("five"))
        val err = ByteArrayOutputStream()
        val env = Environment()

        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
        assertEquals(2, exit.execute(input, out, err, env))
        assertEquals(Environment.ExitStatus.EXITING, env.exitStatus)
    }

}
