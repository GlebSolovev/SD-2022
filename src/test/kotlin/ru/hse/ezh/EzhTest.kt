package ru.hse.ezh

import ru.hse.ezh.exceptions.ViewException
import ru.hse.ezh.execution.commands.utils.CHARSET
import ru.hse.ezh.views.View

import java.io.File
import java.io.InputStream

import kotlin.test.*

class EzhTest {

    private val testDir = File("ezh-test-temp-directory-ezh")
    private val testFile = testDir.resolve("file")

    @BeforeTest
    fun initTestDirectory() {
        testDir.mkdirs()
        testFile.createNewFile()
    }

    @AfterTest
    fun deleteTestDirectory() {
        testFile.delete()
        testDir.delete()
    }

    class MockView(script: List<String>) : View {

        private val outBuilder = StringBuilder()
        private val errBuilder = StringBuilder()

        fun getOut() = outBuilder.toString()
        fun getErr() = errBuilder.toString()

        private val iter = script.iterator()

        override fun getInput() = iter.next().asSequence()

        override fun writeOutput(out: InputStream) {
            outBuilder.append(String(out.readAllBytes(), CHARSET))
        }

        override fun writeError(err: InputStream) {
            errBuilder.append(String(err.readAllBytes(), CHARSET))
        }

    }

    private fun ezhSuccessfulSessionHelper(script: List<String>, expected: Triple<Int, String, String>) {
        val view = MockView(script)
        val (code, out, err) = expected
        assertEquals(code, Ezh(view).main())
        assertEquals(out, view.getOut())
        assertTrue(view.getErr().startsWith(err))
    }

    @Test
    fun testCat() {
        testFile.writeText("hello", CHARSET)
        ezhSuccessfulSessionHelper(
            listOf("cat \"${testFile.canonicalPath}\"", "exit"),
            Triple(0, "hello", "")
        )
    }

    @Test
    fun testCatFails() = ezhSuccessfulSessionHelper(
        listOf("cat ezh-nonexistent-file", "exit"),
        Triple(0, "", "cat: IOException during reading file")
    )

    @Test
    fun testEcho() = ezhSuccessfulSessionHelper(
        listOf("echo blah blah", "exit"),
        Triple(0, "blah blah", "")
    )

    @Test
    fun testExit() = ezhSuccessfulSessionHelper(
        listOf("exit 5", "echo oops"),
        Triple(5, "", "")
    )

    @Test
    fun testPwd() = ezhSuccessfulSessionHelper(
        listOf("pwd", "exit"),
        Triple(0, System.getProperty("user.dir"), "")
    )

    @Test
    fun testPwdFails() = ezhSuccessfulSessionHelper(
        listOf("pwd oops", "exit"),
        Triple(0, "", "pwd: expected zero arguments")
    )

    @Test
    fun testWc() {
        testFile.writeText("hello", CHARSET)
        ezhSuccessfulSessionHelper(
            listOf("wc \"${testFile.canonicalPath}\"", "exit"),
            Triple(0, "1\t1\t5", "")
        )
    }

    @Test
    fun testWcFails() = ezhSuccessfulSessionHelper(
        listOf("wc", "exit"),
        Triple(0, "1\t0\t0", "")
    )

    @Test
    fun testExternal() {
        if (System.getProperty("os.name").startsWith("Windows")) return

        val view = MockView(listOf("bash -c \'echo word\'", "exit"))

        assertEquals(0, Ezh(view).main())
        if (view.getErr().startsWith("bash: could not startup process")) {
            // no bash
            assertEquals("", view.getOut())
        } else {
            // yes bash
            assertEquals("", view.getErr())
            assertEquals("word\n", view.getOut())
        }
    }

    @Test
    fun testExternalUnknown() {
        val view = MockView(listOf("ezh-unknown-command", "exit"))

        assertEquals(0, Ezh(view).main())
        assertTrue(view.getErr().startsWith("ezh-unknown-command: could not startup process"))
        assertEquals("", view.getOut())
    }

    @Test
    fun testExternalWithEnvironment() {
        if (System.getProperty("os.name").startsWith("Windows")) return

        val view = MockView(listOf("x=9", "y=7 | bash -c 'echo \$x 6 3 \$y'", "exit"))

        assertEquals(0, Ezh(view).main())
        if (view.getErr().startsWith("bash: could not startup process")) {
            // no bash
            assertEquals("", view.getOut())
        } else {
            // yes bash
            assertEquals("", view.getErr())
            assertEquals("9 6 3 7\n", view.getOut())
        }
    }

    @Test
    fun testLexerFails() = ezhSuccessfulSessionHelper(
        listOf("echo \"oh no\'", "a = 5", "$=5", "exit"),
        Triple(
            0, "",
            "lexing error: unterminated quotes, at position: 12\n" +
                "lexing error: space near assign is forbidden, at position: unknown\n" +
                "lexing error: empty substitution is forbidden: no variable name, at position: 2\n"
        )
    )

    @Test
    fun testParserFails() = ezhSuccessfulSessionHelper(
        listOf("=5", "5=", "x=5 echo \$x", "|", "exit"),
        Triple(
            0, "",
            "parsing error: empty LHS of assignment, last valid token: null\n" +
                "parsing error: empty RHS of assignment, last valid token: ASSIGN\n" +
                "parsing error: sequential operations without pipe, last valid token: WORD(str=5)\n" +
                "parsing error: pipe must be between two operations, last valid token: null\n"
        )
    )

    @Test
    fun testViewFails() {
        val view = object : View {
            override fun getInput(): Sequence<Char> {
                throw ViewException("nope")
            }

            override fun writeOutput(out: InputStream) {
                throw ViewException("nope")
            }

            override fun writeError(err: InputStream) {
                throw ViewException("nope")
            }
        }
        assertEquals(-1, Ezh(view).main())
    }

    @Test
    fun testPipe() {
        testFile.writeText("hello", CHARSET)
        ezhSuccessfulSessionHelper(
            listOf("echo word | wc", "exit"),
            Triple(0, "1\t1\t4", "")
        )
    }

    @Test
    fun testSimpleCdHome() {
        ezhSuccessfulSessionHelper(
            listOf("cd", "pwd", "exit"),
            Triple(0, System.getProperty("user.home"), "")
        )
    }

    @Test
    fun testCdToDir() {
        ezhSuccessfulSessionHelper(
            listOf("cd \"${testDir.canonicalPath}\"", "pwd", "cd ..", "pwd", "cd \"${testDir.name}\"", "pwd", "exit"),
            Triple(
                0,
                "${testDir.canonicalPath}${testDir.absoluteFile.parentFile.canonicalPath}${testDir.canonicalPath}",
                ""
            )
        )
    }

    @Test
    fun testCdToDirAndBack() {
        val tmpDir = testDir.resolve("tmp")
        tmpDir.mkdirs()
        ezhSuccessfulSessionHelper(
            listOf("cd \"${tmpDir.canonicalPath}\"", "cd ..", "cd .", "pwd", "exit"),
            Triple(0, testDir.canonicalPath, "")
        )
        tmpDir.delete()
    }

    @Test
    fun testCdFail() {
        val old = System.getProperty("user.dir")
        ezhSuccessfulSessionHelper(
            listOf("cd \"${testFile.canonicalPath}\"", "exit"),
            Triple(0, "", "cd: ${testFile.canonicalPath} is not a directory")
        )
        val fake = File("I DO NOT EXIST FOR SURE")
        ezhSuccessfulSessionHelper(
            listOf("cd \"${fake.canonicalPath}\"", "pwd", "exit"),
            Triple(0, old, "cd: File or directory ${fake.canonicalPath} does not exist")
        )
        ezhSuccessfulSessionHelper(
            listOf("cd abc ncd", "pwd", "exit"),
            Triple(0, old, "cd: Got too much arguments. Expected 0 or 1")
        )
    }

    @Test
    fun testLsFail() {
        val fake = File("I DO NOT EXIST FOR SURE")
        ezhSuccessfulSessionHelper(
            listOf("ls \"${fake.canonicalPath}\"", "exit"),
            Triple(0, "", "ls: File or directory ${fake.canonicalPath} does not exist")
        )
    }

    @Test
    fun testLsFile() {
        ezhSuccessfulSessionHelper(
            listOf("ls \"${testFile.canonicalPath}\"", "exit"),
            Triple(0, "${testFile.canonicalPath}\n", "")
        )
    }

    @Test
    fun testLsDirWithSingleFile() {
        ezhSuccessfulSessionHelper(
            listOf("ls \"${testDir.canonicalPath}\"", "exit"),
            Triple(0, "${testFile.name}\n", "")
        )
    }

    @Test
    fun testLsEmptyDir() {
        val emptyDir = File("ezh-test-temp-empty-directory-ezh")
        emptyDir.mkdirs()
        ezhSuccessfulSessionHelper(
            listOf("ls \"${emptyDir.canonicalPath}\"", "exit"),
            Triple(0, "", "")
        )
        emptyDir.delete()
    }

    @Test
    fun testLsDirWithMultipleFilesAndCd() {
        val fileA = testDir.resolve("A")
        val fileB = testDir.resolve("B")
        val fileC = testDir.resolve("C")
        val fileZ = testDir.resolve("Z")
        fileA.createNewFile()
        fileB.createNewFile()
        fileC.createNewFile()
        fileZ.createNewFile()

        ezhSuccessfulSessionHelper(
            listOf("ls \"${testDir.canonicalPath}\"", "exit"),
            Triple(0, "A\nB\nC\nfile\nZ\n", "")
        )

        ezhSuccessfulSessionHelper(
            listOf("cd \"${testDir.canonicalPath}\"", "ls", "exit"),
            Triple(0, "A\nB\nC\nfile\nZ\n", "")
        )

        fileA.delete()
        fileB.delete()
        fileC.delete()
        fileZ.delete()
    }

    @Test
    fun testCdWithOtherCommands() {
        testFile.writeText("hello", CHARSET)
        ezhSuccessfulSessionHelper(
            listOf(
                "cd \"${testDir.canonicalPath}\"",
                "wc \"${testFile.name}\"",
                "cat \"${testFile.name}\"",
                "cd ..",
                "wc \"${testDir.name + File.separator + testFile.name}\"",
                "cat \"${testDir.name + File.separator + testFile.name}\"",
                "exit"
            ),
            Triple(0, "1\t1\t5hello1\t1\t5hello", "")
        )
    }
}
