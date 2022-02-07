package ru.hse.ezh.parsing

import org.junit.jupiter.api.assertThrows
import ru.hse.ezh.exceptions.EmptyLHSException
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
    fun testAssignAfterAssignment() {
        val input = listOf(WORD("var"), ASSIGN, WORD("value"), ASSIGN, WORD("another value"))
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
        val input = listOf(WORD("echo"), SPACE, WORD("word"))
        assertThrows<IllegalArgumentException> { Parser.parse(input) }
    }

}
