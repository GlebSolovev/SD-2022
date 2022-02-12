package ru.hse.ezh.parsing

import org.junit.jupiter.api.assertThrows
import ru.hse.ezh.exceptions.EmptyLHSException
import ru.hse.ezh.exceptions.EmptyPipeException
import ru.hse.ezh.exceptions.EmptyRHSException
import ru.hse.ezh.exceptions.NotPipedOperationsException
import ru.hse.ezh.execution.Assignment
import ru.hse.ezh.execution.commands.*

import java.lang.IllegalArgumentException

import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun testAssignment() {
        val input = listOf(WORD("var"), ASSIGN, WORD("value"))
        val expected = listOf(Assignment(WORD("var"), WORD("value")))

        assertEquals(expected, Parser.parse(input))
    }

    private fun testCommandHelper(vararg commandWithArguments: String) {
        val input = commandWithArguments.map { WORD(it) }
        val expected = (Parser.knownCommands[input[0]]!!)(commandWithArguments.toList().drop(1))

        assertEquals(listOf(expected), Parser.parse(input))
    }

    @Test
    fun testCatCommand() = testCommandHelper("cat", "file")

    @Test
    fun testEchoCommand() = testCommandHelper("echo", "some", "text")

    @Test
    fun testExitCommand() = testCommandHelper("exit", "5")

    @Test
    fun testPwdCommand() = testCommandHelper("pwd")

    @Test
    fun testWcCommand() = testCommandHelper("wc", "file")

    @Test
    fun testExternalCommand() {
        val input = listOf(WORD("external-command"), WORD("first"), WORD("second"))
        val expected = listOf(ExternalCommand("external-command", listOf("first", "second")))

        assertEquals(expected, Parser.parse(input))
    }

    @Test
    fun testEmptyList() {
        assertEquals(emptyList(), Parser.parse(emptyList()))
    }

    @Test
    fun testInitialAssign() {
        val input = listOf(ASSIGN)
        assertThrows<EmptyLHSException> { Parser.parse(input) }
    }

    @Test
    fun testEmptyRhsError() {
        val input = listOf(WORD("var"), ASSIGN)
        assertThrows<EmptyRHSException> { Parser.parse(input) }
    }

    @Test
    fun testEmptyRhsErrorBeforePipe() {
        val input = listOf(WORD("var"), ASSIGN, PIPE)
        assertThrows<EmptyRHSException> { Parser.parse(input) }
    }

    @Test
    fun testDoubleAssigns() {
        val input = listOf(WORD("var"), ASSIGN, ASSIGN)
        assertThrows<EmptyRHSException> { Parser.parse(input) }
    }

    @Test
    fun testAssignAfterAssignment() {
        val input = listOf(WORD("var"), ASSIGN, WORD("value"), ASSIGN, WORD("another value"))
        assertThrows<EmptyLHSException> { Parser.parse(input) }
    }

    @Test
    fun testEmptyLhsErrorAfterPipe() {
        val input = listOf(WORD("exit"), PIPE, ASSIGN)
        assertThrows<EmptyLHSException> { Parser.parse(input) }
    }

    @Test
    fun testNotPipedOperations() {
        val inputCommandAfterAssignment = listOf(WORD("var"), ASSIGN, WORD("value"), WORD("pwd"))
        assertThrows<NotPipedOperationsException> { Parser.parse(inputCommandAfterAssignment) }

        val inputAssignmentAfterCommand = listOf(WORD("echo"), WORD("var"), ASSIGN, WORD("value"))
        assertThrows<NotPipedOperationsException> { Parser.parse(inputAssignmentAfterCommand) }
    }

    @Test
    fun testInvalidTokens() {
        assertThrows<IllegalArgumentException> { Parser.parse(listOf(SPACE)) }
        assertThrows<IllegalArgumentException> { Parser.parse(listOf(WORD("echo"), SUBST("x"))) }
        assertThrows<IllegalArgumentException> { Parser.parse(listOf(WORD("exit"), WORD("5"), QSUBST("x"))) }
        assertThrows<IllegalArgumentException> { Parser.parse(listOf(WORD("x"), ASSIGN, SPACE)) }
        assertThrows<IllegalArgumentException> { Parser.parse(listOf(WORD("x"), ASSIGN, WORD("ex"), SPACE)) }
        assertThrows<IllegalArgumentException> { Parser.parse(listOf(WORD("exit"), PIPE, SPACE)) }
    }

    @Test
    fun testPipedCommands() {
        val input = listOf(WORD("echo"), WORD("word"), PIPE, WORD("wc"), PIPE, WORD("cat"))
        val expected = listOf(EchoCommand(listOf("word")), WcCommand(emptyList()), CatCommand(emptyList()))

        assertEquals(expected, Parser.parse(input))
    }

    @Test
    fun testPipedAssignments() {
        val input = listOf(WORD("x"), ASSIGN, WORD("ex"), PIPE, WORD("y"), ASSIGN, WORD("it"))
        val expected = listOf(Assignment(WORD("x"), WORD("ex")), Assignment(WORD("y"), WORD("it")))

        assertEquals(expected, Parser.parse(input))
    }

    @Test
    fun testPipedOperations() {
        val input = listOf(WORD("x"), ASSIGN, WORD("ex"), PIPE, WORD("exit"), PIPE, WORD("y"), ASSIGN, WORD("it"))
        val expected =
            listOf(Assignment(WORD("x"), WORD("ex")), ExitCommand(emptyList()), Assignment(WORD("y"), WORD("it")))

        assertEquals(expected, Parser.parse(input))
    }

    @Test
    fun testEmptyPipedError() {
        assertThrows<EmptyPipeException> { Parser.parse(listOf(PIPE)) }
        assertThrows<EmptyPipeException> { Parser.parse(listOf(WORD("exit"), PIPE)) }
        assertThrows<EmptyPipeException> { Parser.parse(listOf(WORD("exit"), PIPE, PIPE)) }
    }

    @Test
    fun testCommandNameAssignment() {
        val input = listOf(WORD("exit"), ASSIGN, WORD("5"))
        val expected = listOf(Assignment(WORD("exit"), WORD("5")))

        assertEquals(expected, Parser.parse(input))
    }

}
