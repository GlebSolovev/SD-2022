package ru.hse.ezh.execution

import ru.hse.ezh.Environment
import ru.hse.ezh.execution.commands.ExitCommand
import ru.hse.ezh.execution.commands.utils.CHARSET
import ru.hse.ezh.parsing.WORD

import java.io.InputStream
import java.io.OutputStream

import kotlin.test.Test
import kotlin.test.assertEquals

private fun InputStream.readString() = String(this.readAllBytes())
private fun OutputStream.writeString(str: String) = this.write(str.toByteArray(CHARSET))

class ExecutorTest {

    @Test
    fun testAssignment() {
        val assignment = Assignment(WORD("var"), WORD("value"))
        val env = Environment()
        val (code, out, err) = Executor.execute(listOf(assignment), env)

        assertEquals(0, code)
        assertEquals("", out.readString())
        assertEquals("", err.readString())
        assertEquals("value", env.getVariable("var"))
    }

    @Test
    fun testExit() {
        val exit = ExitCommand(listOf("5"))
        val env = Environment()
        val (code, out, err) = Executor.execute(listOf(exit), env)

        assertEquals(5, code)
        assertEquals("", out.readString())
        assertEquals("", err.readString())
        assertEquals(Environment.ExitStatus.EXITING, env.exitStatus)
    }

    @Test
    fun testEmptyArguments() {
        val env = Environment()
        val (code, out, err) = Executor.execute(listOf(), env)

        assertEquals(0, code)
        assertEquals("", out.readString())
        assertEquals("", err.readString())
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
    }

    class MockCommand(val id: Int, args: List<String>) : Command(args) {
        override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
            input.transferTo(out)
            out.writeString(id.toString())
            err.writeString(id.toString())
            env.putVariable(id.toString(), id.toString())
            return id
        }
    }

    @Test
    fun testCommand() {
        val command = MockCommand(1, listOf())
        val env = Environment()
        val (code, out, err) = Executor.execute(listOf(command), env)

        assertEquals(1, code)
        assertEquals("1", out.readString())
        assertEquals("1", err.readString())
        assertEquals("1", env.getVariable("1"))
    }

}
