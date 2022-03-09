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
        val (code, out, err) = Executor.execute(emptyList(), env)

        assertEquals(0, code)
        assertEquals("", out.readString())
        assertEquals("", err.readString())
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
    }

    class MockCommand(private val id: Int, args: List<String>) : Command(args) {
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
        val command = MockCommand(1, emptyList())
        val env = Environment()
        val (code, out, err) = Executor.execute(listOf(command), env)

        assertEquals(1, code)
        assertEquals("1", out.readString())
        assertEquals("1", err.readString())
        assertEquals("1", env.getVariable("1"))
    }

    @Test
    fun testPipeCommands() {
        val env = Environment()
        val (code, out, err) = Executor.execute(
            listOf(
                MockCommand(1, emptyList()),
                MockCommand(2, emptyList()),
                MockCommand(3, emptyList()),
            ),
            env
        )

        assertEquals(3, code)
        assertEquals("123", out.readString())
        assertEquals("123", err.readString())
        assertEquals(emptyMap(), env.getAllVariables())
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
    }

    @Test
    fun testPipeAssignments() {
        val env = Environment()
        val (code, out, err) = Executor.execute(
            listOf(
                Assignment(WORD("1"), WORD("1")),
                Assignment(WORD("2"), WORD("2")),
                Assignment(WORD("3"), WORD("3")),
            ),
            env
        )

        assertEquals(0, code)
        assertEquals("", out.readString())
        assertEquals("", err.readString())
        assertEquals(emptyMap(), env.getAllVariables())
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
    }

    @Test
    fun testPipeEnvironment() {
        val mockCommand = object : Command(emptyList()) {
            override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
                env.getAllVariables().asSequence()
                    .map { (k, v) -> "$k $v" }
                    .sorted()
                    .forEach { out.writeString(it + "\n") }
                return 0
            }
        }

        val env = Environment()
        env.putVariable("0", "0")
        val (code, out, err) = Executor.execute(
            listOf(
                mockCommand,
                Assignment(WORD("1"), WORD("1")),
                mockCommand,
            ),
            env
        )

        assertEquals(0, code)
        assertEquals("0 0\n1 1\n", out.readString())
        assertEquals("", err.readString())
        assertEquals(mapOf("0" to "0"), env.getAllVariables())
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
    }

    @Test
    fun testPipeAssignGlobalEnvironment() {
        val env = Environment()
        val (code, out, err) = Executor.execute(
            listOf(Assignment(WORD("1"), WORD("1"))), env
        )

        assertEquals(0, code)
        assertEquals("", out.readString())
        assertEquals("", err.readString())
        assertEquals(mapOf("1" to "1"), env.getAllVariables())
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
    }

    @Test
    fun testPipeExitLocalEnvironment() {
        val env = Environment()
        val (code, out, err) = Executor.execute(
            listOf(
                MockCommand(1, emptyList()),
                ExitCommand(listOf("5")),
                MockCommand(2, emptyList()),
                Assignment(WORD("var"), WORD("value"))
            ),
            env
        )

        assertEquals(5, code)
        assertEquals("", out.readString())
        assertEquals("1", err.readString())
        assertEquals(emptyMap(), env.getAllVariables())
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
    }

    @Test
    fun testPipeExitGlobalEnvironment() {
        val env = Environment()
        val (code, out, err) = Executor.execute(
            listOf(ExitCommand(listOf("5"))), env
        )

        assertEquals(5, code)
        assertEquals("", out.readString())
        assertEquals("", err.readString())
        assertEquals(Environment.ExitStatus.EXITING, env.exitStatus)
    }

}
