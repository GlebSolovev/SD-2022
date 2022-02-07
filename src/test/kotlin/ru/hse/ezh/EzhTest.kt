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
            listOf("cat ${testFile.canonicalPath}", "exit"),
            Triple(0, "hello\n", "")
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
        Triple(0, "blah blah\n", "")
    )

    @Test
    fun testExit() = ezhSuccessfulSessionHelper(
        listOf("exit 5", "echo oops"),
        Triple(5, "", "")
    )

    @Test
    fun testPwd() = ezhSuccessfulSessionHelper(
        listOf("pwd", "exit"),
        Triple(0, System.getProperty("user.dir") + "\n", "")
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
            listOf("wc ${testFile.canonicalPath}", "exit"),
            Triple(0, "1\t1\t5\n", "")
        )
    }

    @Test
    fun testWcFails() = ezhSuccessfulSessionHelper(
        listOf("wc", "exit"),
        Triple(0, "1\t0\t0\n", "")
    )

// TODO: ProcessBuilder does not pass the environment for mysterious reasons
//    @Test
//    fun testExternal() {
//        val view = MockView(listOf("x=9", "y=7", "bash -c 'echo \$x 6 3 \$y'", "exit"))
//
//        assertEquals(0, Ezh(view).main())
//        if (view.getErr().startsWith("bash: could not startup process")) {
//            // no bash
//            assertEquals("", view.getOut())
//        } else {
//            // yes bash
//            assertEquals("", view.getErr())
//            assertEquals("9 6 3 7\n", view.getOut())
//        }
//    }

    @Test
    fun testLexerFails() = ezhSuccessfulSessionHelper(
        listOf("echo \"oh no\'", "a = 5", "exit"),
        Triple(
            0, "",
            "lexing error: unterminated quotes, at position: 12\n" +
                "lexing error: space near assign is forbidden, at position: unknown\n"
        )
    )

    @Test
    fun testParserFails() = ezhSuccessfulSessionHelper(
        listOf("=5", "5=", "x=5 echo \$x", "exit"),
        Triple(
            0, "",
            "parsing error: empty LHS of assignment, last valid token: null\n" +
                "parsing error: empty RHS of assignment, last valid token: ASSIGN\n" +
                "parsing error: sequential operations without pipe, last valid token: WORD(str=5)\n"
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

}
