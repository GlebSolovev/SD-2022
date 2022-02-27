package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.execution.commands.utils.CHARSET

import java.io.*

import kotlin.test.*

class CatCommandTest {

    private val env = Environment()
    private val testDir = File("ezh-test-temp-directory-cat")

    @BeforeTest
    fun initTestDirectory() {
        testDir.mkdirs()
    }

    @AfterTest
    fun deleteTestDirectory() {
        testDir.delete()
    }

    @Test
    fun testReadFromInput() {
        val cat = CatCommand(emptyList())
        val input = ByteArrayInputStream("text".toByteArray())
        val out = ByteArrayOutputStream()
        val err = OutputStream.nullOutputStream()

        assertEquals(0, cat.execute(input, out, err, env))
        assertEquals("text", out.toString(CHARSET))
    }

    @Test
    fun testReadFromStrangeInput() {
        val cat = CatCommand(emptyList())
        val input = ByteArrayInputStream(" \t\r\n ё\u9637\"\n".toByteArray())
        val out = ByteArrayOutputStream()
        val err = OutputStream.nullOutputStream()

        assertEquals(0, cat.execute(input, out, err, env))
        assertEquals(" \t\r\n ё\u9637\"\n", out.toString(CHARSET))
    }

    @Test
    fun testReadFromFile() {
        val file = testDir.resolve("file")
        file.writeText("text")

        val cat = CatCommand(listOf(file.canonicalPath))
        val input = InputStream.nullInputStream()
        val out = ByteArrayOutputStream()
        val err = OutputStream.nullOutputStream()

        assertEquals(0, cat.execute(input, out, err, env))
        assertEquals("text", out.toString(CHARSET))

        file.delete()
    }

    @Test
    fun testReadFromStrangeFile() {
        val file = testDir.resolve("strange-file")
        file.writeText(" \t\r\n ё\u9637\"\n")

        val cat = CatCommand(listOf(file.canonicalPath))
        val input = InputStream.nullInputStream()
        val out = ByteArrayOutputStream()
        val err = OutputStream.nullOutputStream()

        assertEquals(0, cat.execute(input, out, err, env))
        assertEquals(" \t\r\n ё\u9637\"\n", out.toString(CHARSET))

        file.delete()
    }

    @Test
    fun testReadFromNotExistingFile() {
        val cat = CatCommand(listOf(testDir.resolve("non-existing-file").canonicalPath))
        val input = InputStream.nullInputStream()
        val out = OutputStream.nullOutputStream()
        val err = ByteArrayOutputStream()

        assertEquals(2, cat.execute(input, out, err, env))
        assertTrue(err.toString(CHARSET).startsWith("cat: IOException during reading file\n"))
    }

    @Test
    fun testInvalidArguments() {
        val cat = CatCommand(listOf("filename", "something"))
        val input = InputStream.nullInputStream()
        val out = OutputStream.nullOutputStream()
        val err = ByteArrayOutputStream()

        assertEquals(1, cat.execute(input, out, err, env))
        assertEquals("cat: expected one or zero arguments\n", err.toString(CHARSET))
    }

}
