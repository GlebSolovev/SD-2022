package ru.hse.ezh.execution.commands

import ru.hse.ezh.Environment
import ru.hse.ezh.execution.commands.utils.CHARSET

import java.io.*

import kotlin.test.*

class GrepCommandTest {

    private val env = Environment()
    private val testDir = File("ezh-test-temp-directory-grep")

    @BeforeTest
    fun initTestDirectory() {
        testDir.mkdirs()
    }

    @AfterTest
    fun deleteTestDirectory() {
        testDir.delete()
    }

    private fun testSuccessHelper(expectedOutput: String, input: InputStream, arguments: List<String>) {
        val grep = GrepCommand(arguments)
        val out = ByteArrayOutputStream()
        val err = OutputStream.nullOutputStream()

        assertEquals(0, grep.execute(input, out, err, env))
        assertEquals(expectedOutput, out.toString(CHARSET))
    }

    private fun testErrorHelper(
        expectedCode: Int,
        expectedErrStart: String,
        input: InputStream,
        arguments: List<String>
    ) {
        val grep = GrepCommand(arguments)
        val out = OutputStream.nullOutputStream()
        val err = ByteArrayOutputStream()

        assertEquals(expectedCode, grep.execute(input, out, err, env))
        assertTrue(err.toString(CHARSET).startsWith(expectedErrStart))
    }

    private fun testReadFromInputHelper(expectedOutput: String, inputContent: String, arguments: List<String>) {
        val input = ByteArrayInputStream(inputContent.toByteArray())
        testSuccessHelper(expectedOutput, input, arguments)
    }

    private fun testErrorFromInputHelper(
        expectedCode: Int,
        expectedErrStart: String,
        arguments: List<String>
    ) {
        val input = ByteArrayInputStream("".toByteArray())
        testErrorHelper(expectedCode, expectedErrStart, input, arguments)
    }

    private fun testReadFromFileHelper(expectedOutput: String, inputContent: String, arguments: List<String>) {
        val file = testDir.resolve("file")
        file.writeText(inputContent)

        val argumentsWithFile: MutableList<String> = arguments.toMutableList()
        argumentsWithFile.add(file.canonicalPath)
        val input = InputStream.nullInputStream()

        testSuccessHelper(expectedOutput, input, argumentsWithFile)
        file.delete()
    }

    @Suppress("SameParameterValue")
    private fun testErrorFromFileHelper(
        expectedCode: Int,
        expectedErrStart: String,
        arguments: List<String>
    ) {
        val file = testDir.resolve("file")
        file.writeText("")

        val argumentsWithFile: MutableList<String> = arguments.toMutableList()
        argumentsWithFile.add(file.canonicalPath)
        val input = InputStream.nullInputStream()

        testErrorHelper(expectedCode, expectedErrStart, input, argumentsWithFile)
        file.delete()
    }

    private fun testReadHelper(expectedOutput: String, inputContent: String, arguments: List<String>) {
        testReadFromInputHelper(expectedOutput, inputContent, arguments)
        testReadFromFileHelper(expectedOutput, inputContent, arguments)
    }

    @Test
    fun testNoFlagsSingleLineMatch() = testReadHelper("some word in world", "some word in world", listOf("word"))

    @Test
    fun testNoFlagsSingleLineMultiMatch() =
        testReadHelper("some word and word in words", "some word and word in words", listOf("word"))

    @Test
    fun testNoFlagsSingleLineMismatch() = testReadHelper("", "some thing in world", listOf("word"))

    @Test
    fun testNoFlagsEmptyInput() = testReadHelper("", "", listOf("word"))

    @Test
    fun testNoFlagsMultiLine() =
        testReadHelper("some word in world\nword", "some word in world\nworld\nword", listOf("word"))

    @Test
    fun testNoFlagsMatchAtEndings() = testReadHelper("some word\nword some", "some word\nword some", listOf("word"))

    @Test
    fun testNoFlagsCaseSensitive() = testReadHelper("wOrd", "Word\nword\nWORD\nwOrd", listOf("wOrd"))

    @Test
    fun testNoFlagsEmptyLines() = testReadHelper("word\nword", "\n\nword\n\n\nword\n", listOf("word"))

    @Test
    fun testNoFlagsStrangeLineDelimiters() =
        testReadHelper("some\tword\nword\nother word", "some\tword\n\ranother\rword\r\nother word\n", listOf("word"))

    @Test
    fun testNoFlagsStrangeSymbols() =
        testReadHelper("\u9637 ё\t", "\u9637 ё\t\n\u9636 ё\t\n\u9636\t", listOf("\u9637 ё\t"))

    @Test
    fun testNoFlagsRegexPatterns() {
        testReadHelper("word", "word 1\n2 word\nword", listOf("^word$"))
        testReadHelper("vord 5", "vord 5\nword5\nword  5\n ord 5\nword", listOf("\\word\\s\\d"))
        testReadHelper("word\nw_rd\nw_r", "word\n_ord\nw_rd\nw_r\nwo\nward", listOf("[a-zA-Z][^ard][rd]+"))
        testReadHelper("o\nword\na\n\nworddro", "o\nword\na\n\nworddro", listOf("w?[ord]*"))
    }

    @Test
    fun testUselessMultilineRegexPattern() {
        testReadHelper("", "word\nword", listOf("word\nword"))
    }

    @Test
    fun testInvalidRegexPattern() {
        testErrorFromInputHelper(3, "grep: invalid pattern:", listOf("["))
        testErrorFromFileHelper(3, "grep: invalid pattern:", listOf("["))
    }

    @Test
    fun testReadFromNotExistingFile() {
        val grep = GrepCommand(listOf("word", testDir.resolve("non-existing-file").canonicalPath))
        val input = InputStream.nullInputStream()
        val out = OutputStream.nullOutputStream()
        val err = ByteArrayOutputStream()

        assertEquals(1, grep.execute(input, out, err, env))
        assertTrue(err.toString(CHARSET).startsWith("grep: invalid arguments: "))
    }

    @Test
    fun testInvalidCLIArguments() {
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf())
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf("pattern", "-u"))
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf("pattern", "pattern", "filename"))
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf("pattern", "-A"))
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf("pattern", "-A", "-1"))
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf("pattern", "-A", "-A", "5"))
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf("pattern", "filename", "-A", "ten"))
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf("pattern", "filename", "-w", "param"))
        testErrorFromInputHelper(1, "grep: invalid arguments: ", listOf("pattern", "--w"))
    }

    @Test
    fun testValidCLIArguments() {
        testReadFromInputHelper("", "", listOf("pattern", "-A5"))
        testReadFromInputHelper("", "", listOf("pattern", "-w", "-w"))
        testReadFromInputHelper("", "", listOf("pattern", "-wi"))
        testReadFromInputHelper("", "", listOf("pattern", "-wiA", "5"))
        testReadFromInputHelper("", "", listOf("-A", "5", "pattern", "-w", "-i"))
    }

    @Test
    fun testCaseInsensitiveFlag() {
        testReadHelper("word\nWord\nwOrd\nWORD", "word\nWord\nwOrd\nWORD\nward", listOf("word", "-i"))
    }

    @Test
    fun testWordSearchFlag() =
        testReadHelper(" word \nword\t", "wordword\n word \nword_\nword\t\nWord", listOf("word", "-w"))

    @Test
    fun testWordSearchFlagStrangeWordDelimiters() = testReadHelper(
        "word  word\nword\tword\nword\nword\nword\t\tword",
        "word  word\nword\tword\nword\rword\nword\t\tword\nword\u9637word",
        listOf("word", "-w")
    )

    @Test
    fun testFollowingLinesFlagNonOverlappingZero() {
        testReadHelper("", "", listOf("word", "-A", "0"))
        testReadHelper("word\n--\nword", "word\nnot\nword", listOf("word", "-A", "0"))
    }

    @Test
    fun testFollowingLinesFlagNonOverlappingMulti() {
        testReadHelper("", "", listOf("word", "-A", "1"))
        testReadHelper("word\nnot\n--\nword", "word\nnot\nother\nword", listOf("word", "-A", "1"))
        testReadHelper("word\nnot\n--\nword", "ward\nword\nnot\nother\nword", listOf("word", "-A", "1"))
        testReadHelper("word\nnot\nother\n--\nword", "word\nnot\nother\nnot\nword", listOf("word", "-A", "2"))
    }

    @Test
    fun testFollowingLinesFlagOverlappingZero() =
        testReadHelper("word\nword\n--\nword", "word\nword\nnot\nword", listOf("word", "-A", "0"))

    @Test
    fun testFollowingLinesFlagOverlappingMulti() {
        testReadHelper(
            "word\nnot\nword\nnot\n--\nword",
            "word\nnot\nword\nnot\nother\nword",
            listOf("word", "-A", "1")
        )
        testReadHelper(
            "word\nnot\nword\nnot\n--\nword",
            "ward\nword\nnot\nword\nnot\nother\nword",
            listOf("word", "-A", "1")
        )
        testReadHelper(
            "word\nnot\nword\nnot\nother\nword\n\n\n--\nword",
            "word\nnot\nword\nnot\nother\nword\n\n\n\nword",
            listOf("word", "-A", "2")
        )
    }

}
