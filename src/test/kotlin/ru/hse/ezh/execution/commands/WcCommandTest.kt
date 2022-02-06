package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment

import java.io.*

import kotlin.test.*

class WcCommandTest {

    private val env = Environment()
    private val testDir = File("ezh-test-temp-directory-wc")

    @BeforeTest
    fun initTestDirectory() {
        testDir.mkdirs()
    }

    @AfterTest
    fun deleteTestDirectory() {
        testDir.delete()
    }

    private fun testReadFromInputHelper(expectedOutput: String, inputString: String) {
        val wc = WcCommand(listOf())
        val input = ByteArrayInputStream(inputString.toByteArray())
        val out = ByteArrayOutputStream()
        val err = OutputStream.nullOutputStream()

        assertEquals(0, wc.execute(input, out, err, env))
        assertEquals(expectedOutput, out.toString())
    }

    private fun testReadFromFileHelper(expectedOutput: String, inputString: String) {
        val file = testDir.resolve("file")
        file.writeText(inputString)

        val wc = WcCommand(listOf(file.canonicalPath))
        val input = InputStream.nullInputStream()
        val out = ByteArrayOutputStream()
        val err = OutputStream.nullOutputStream()

        assertEquals(0, wc.execute(input, out, err, env))
        assertEquals(expectedOutput, out.toString())

        file.delete()
    }

    private fun testReadHelper(expectedOutput: String, inputString: String) {
        testReadFromInputHelper(expectedOutput, inputString)
        testReadFromFileHelper(expectedOutput, inputString)
    }

    @Test
    fun testReadFromSimpleInput() = testReadHelper("1\t1\t4\n", "text")

    @Test
    fun testReadFromMultipleSpacesInput() = testReadHelper("1\t3\t6\n", "a b  c")

    @Test
    fun testReadFromLineBreakEndingInput() = testReadHelper("3\t2\t4\n", "a\nb\n")

    @Test
    fun testReadFromStrangeCharactersInput() = testReadHelper("2\t2\t6\n", "ё\n\u9637")

    @Test
    fun testReadFromInputWithTabsAndCarets() = testReadHelper("3\t3\t9\n", "a\t\r\n\t\tc\rd")

    @Test
    fun testReadFromStrangeInput() = testReadHelper("3\t2\t16\n", " \t\r\n    ё \u9637\"\n")

    @Test
    fun testReadFromNotExistingFile() {
        val wc = WcCommand(listOf(testDir.resolve("non-existing-file").canonicalPath))
        val input = InputStream.nullInputStream()
        val out = OutputStream.nullOutputStream()
        val err = ByteArrayOutputStream()

        assertEquals(2, wc.execute(input, out, err, env))
        assertTrue(err.toString().startsWith("wc: IOException during reading file\n"))
    }

    @Test
    fun testInvalidArguments() {
        val wc = WcCommand(listOf("filename", "something"))
        val input = InputStream.nullInputStream()
        val out = OutputStream.nullOutputStream()
        val err = ByteArrayOutputStream()

        assertEquals(1, wc.execute(input, out, err, env))
        assertEquals("wc: expected one or zero arguments\n", err.toString())
    }

}
