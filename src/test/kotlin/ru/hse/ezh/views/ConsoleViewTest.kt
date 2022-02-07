package ru.hse.ezh.views

import ru.hse.ezh.execution.commands.utils.CHARSET

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

import kotlin.test.Test
import kotlin.test.assertEquals

class ConsoleViewTest {

    private fun testInputHelper(expected: String, inputString: String) {
        val view = ConsoleView()
        val input = ByteArrayInputStream(inputString.toByteArray(CHARSET))
        System.setIn(input)
        assertEquals(expected, view.getInput().joinToString(separator = ""))
    }

    @Test
    fun testSingleLine() = testInputHelper("pwd", "pwd\n")

    @Test
    fun testMultipleLines() = testInputHelper("pwd", "pwd\necho next")

    @Test
    fun testFullyQuotedLineBreak() = testInputHelper(
        "echo \'some\nfunny\'",
        "echo \'some\nfunny\'\npwd"
    )

    @Test
    fun testWeaklyQuotedLineBreak() = testInputHelper(
        "echo \"some\nfunny\"",
        "echo \"some\nfunny\"\npwd"
    )

    @Test
    fun testQuotedQuotes() = testInputHelper(
        "echo \'some\"\n\"funny\' \"some\'\n\'funny\"",
        "echo \'some\"\n\"funny\' \"some\'\n\'funny\"\npwd",
    )

    @Test
    fun testWriteOutput() {
        val view = ConsoleView()
        val str = "command: its output\n"

        val input = ByteArrayInputStream(str.toByteArray(CHARSET))
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))

        view.writeOutput(input)
        assertEquals(str, out.toString(CHARSET))
    }

    @Test
    fun testWriteError() {
        val view = ConsoleView()
        val str = "command: its error\ncause: detailed"

        val input = ByteArrayInputStream(str.toByteArray(CHARSET))
        val err = ByteArrayOutputStream()
        System.setErr(PrintStream(err))

        view.writeError(input)
        assertEquals(str, err.toString(CHARSET))
    }

}
