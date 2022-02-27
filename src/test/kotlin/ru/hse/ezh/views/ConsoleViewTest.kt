package ru.hse.ezh.views

import ru.hse.ezh.execution.commands.utils.CHARSET

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

import kotlin.test.Test
import kotlin.test.assertEquals

class ConsoleViewTest {

    private val ansiReset = "\u001B[0m"
    private val ansiPurple = "\u001B[35m"
    private val ezhPrefix = "Ё > "
    private val multilinePrefix = "> "

    private val consoleStandardPrefix = ansiPurple + ezhPrefix + ansiReset
    private val consoleMultilinePrefix = ansiPurple + multilinePrefix + ansiReset

    private fun testInputHelper(expected: String, expectedConsolePrefix: String, inputString: String) {
        val view = ConsoleView()

        val input = ByteArrayInputStream(inputString.toByteArray(CHARSET))
        val out = ByteArrayOutputStream()
        System.setIn(input)
        System.setOut(PrintStream(out))

        assertEquals(expected, view.getInput().joinToString(separator = ""))
        assertEquals(expectedConsolePrefix, out.toString(CHARSET))
    }

    @Test
    fun testSingleLine() = testInputHelper("pwd", consoleStandardPrefix, "pwd\n")

    @Test
    fun testMultipleLines() = testInputHelper("pwd", consoleStandardPrefix, "pwd\necho next")

    @Test
    fun testFullyQuotedLineBreak() = testInputHelper(
        "echo \'some\nfunny\'",
        consoleStandardPrefix + consoleMultilinePrefix,
        "echo \'some\nfunny\'\npwd"
    )

    @Test
    fun testWeaklyQuotedLineBreak() = testInputHelper(
        "echo \"some\nfunny\"",
        consoleStandardPrefix + consoleMultilinePrefix,
        "echo \"some\nfunny\"\npwd"
    )

    @Test
    fun testQuotedQuotes() = testInputHelper(
        "echo \'some\"\n\"funny\' \"some\'\n\'funny\"",
        consoleStandardPrefix + consoleMultilinePrefix + consoleMultilinePrefix,
        "echo \'some\"\n\"funny\' \"some\'\n\'funny\"\npwd",
    )

    @Test
    fun testWriteOutput() {
        val view = ConsoleView()
        val str = "command: its output"

        val input = ByteArrayInputStream(str.toByteArray(CHARSET))
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))

        view.writeOutput(input)
        assertEquals(str + "\n", out.toString(CHARSET))
    }

    @Test
    fun testWriteEmptyOutput() {
        val view = ConsoleView()

        val input = ByteArrayInputStream("".toByteArray(CHARSET))
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))

        view.writeOutput(input)
        assertEquals("", out.toString(CHARSET))
    }

    @Test
    fun testWriteError() {
        val view = ConsoleView()
        val str = "command: its error\ncause: detailed\n"

        val input = ByteArrayInputStream(str.toByteArray(CHARSET))
        val err = ByteArrayOutputStream()
        System.setErr(PrintStream(err))

        view.writeError(input)
        assertEquals(str, err.toString(CHARSET))
    }

}
