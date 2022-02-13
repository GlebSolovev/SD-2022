package ru.hse.ezh.execution.commands

import org.junit.jupiter.api.assertThrows
import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.CommandStartupException
import ru.hse.ezh.execution.commands.utils.CHARSET

import java.io.*
import java.util.concurrent.TimeUnit

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExternalCommandTest {

    private val testDir = File("ezh-test-temp-directory-external")
    private val file = testDir.resolve("file")

    @BeforeTest
    fun initTestFile() {
        testDir.mkdirs()
    }

    @AfterTest
    fun deleteTestFile() {
        testDir.delete()
    }

    private fun testExternalCommandHelper(
        name: String,
        args: List<String>,
        inputString: String = "",
        envVars: Map<String, String> = mapOf()
    ) {
        val externalCommand = ExternalCommand(name, args)

        val input = ByteArrayInputStream(inputString.toByteArray())
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        file.writeText(inputString)

        val env = Environment()
        envVars.forEach { env.putVariable(it.key, it.value) }

        try {
            val processBuilder = ProcessBuilder(name, *args.toTypedArray())
            processBuilder.environment().putAll(envVars)
            processBuilder.redirectInput(file)
            val proc = processBuilder.start()

            proc.waitFor(6, TimeUnit.SECONDS) // prevent any hanging
            val expectedExitCode = proc.exitValue()
            val expectedOut = ByteArrayOutputStream()
            proc.inputStream.transferTo(expectedOut)
            val expectedErr = ByteArrayOutputStream()
            proc.errorStream.transferTo(expectedErr)

            assertEquals(expectedExitCode, externalCommand.execute(input, out, err, env))
            assertEquals(expectedOut.toString(CHARSET), out.toString(CHARSET))
            assertEquals(expectedErr.toString(CHARSET), err.toString(CHARSET))

        } catch (_: IOException) {
            assertThrows<CommandStartupException> { externalCommand.execute(input, out, err, env) }
        } catch (_: IllegalThreadStateException) {
            // most likely no command with that name was found (which is slow)
            assertThrows<CommandStartupException> { externalCommand.execute(input, out, err, env) }
        }

        file.delete()
    }

    @Test
    fun testJava() = testExternalCommandHelper("java", listOf("--version"))

    @Test
    fun testEcho() = testExternalCommandHelper("echo", listOf("the", "best", "command"))

    @Test
    fun testCatFromInput() = testExternalCommandHelper("cat", emptyList(), "text")

    @Test
    fun testPrintenv() = testExternalCommandHelper("printenv", emptyList())

    @Test
    fun testBashInLocalEnvironment() =
        testExternalCommandHelper("bash", listOf("-c", "echo \$x"), envVars = mapOf("x" to "5"))

    @Test
    fun testNonExistingCommand() = testExternalCommandHelper("ezh-non-existing-command", listOf("arg"))

    @Test
    fun testWindowsVerCommand() = testExternalCommandHelper("ver", emptyList())
}
