package ru.hse.ezh.parsing

import org.junit.jupiter.api.assertThrows
import ru.hse.ezh.exceptions.UnterminatedQuotesException

import kotlin.test.Test
import kotlin.test.assertEquals

class LexerLexTest {

    private fun lex(inputString: String): List<Token> = Lexer.lex(inputString.asSequence())

    @Test
    fun testSingleWord() {
        val expected = listOf(WORD("word"))
        assertEquals(expected, lex("word"))
    }

    @Test
    fun testWordsAndSpaces() {
        val expected = listOf(SPACE, WORD("a"), SPACE, WORD("b"), SPACE, WORD("c"), SPACE)
        assertEquals(expected, lex("   a b     c  "))
    }

    @Test
    fun testOnlySpaces() {
        val expected = listOf(SPACE)
        assertEquals(expected, lex("  "))
    }

    @Test
    fun testSpecialTextCharacters() {
        val expected = listOf(
            SPACE, WORD("a"),
            SPACE, WORD("b"),
            SPACE, WORD("c"),
            SPACE, WORD("d"),
            SPACE, WORD("e"), SPACE
        )
        assertEquals(expected, lex("\ta\nb\t\tc\r\nd\r \re\t"))
    }

    @Test
    fun testStrangeCharacters() {
        val expected = listOf(WORD("\u9637"), SPACE, WORD("ё"))
        assertEquals(expected, lex("\u9637 ё"))
    }

    @Test
    fun testSingleFullyQuoted() {
        val expected = listOf(WORD("word"))
        assertEquals(expected, lex("\'word\'"))
    }

    @Test
    fun testSingleWeaklyQuoted() {
        val expected = listOf(WORD("word"))
        assertEquals(expected, lex("\"word\""))
    }

    @Test
    fun testQuotedQuotes() {
        val expectedWeakly = listOf(WORD("a\"b"))
        assertEquals(expectedWeakly, lex("\'a\"b\'"))

        val expectedFully = listOf(WORD("a\'b"))
        assertEquals(expectedFully, lex("\"a\'b\""))
    }

    @Test
    fun testMultipleQuotes() {
        val expected = listOf(WORD("abc"))

        assertEquals(expected, lex("\'a\'b\'c\'"))
        assertEquals(expected, lex("\"a\"b\"c\""))
    }

    @Test
    fun testQuotesAndSpaces() {
        val expected = listOf(SPACE, WORD("a"), SPACE, WORD("b"), SPACE)

        assertEquals(expected, lex(" \"a\" \"b\" "))
        assertEquals(expected, lex(" \'a\' \'b\' "))
    }

    @Test
    fun testEmptyQuotes() {
        val expected = listOf(SPACE, WORD(""), SPACE)

        assertEquals(expected, lex(" \"\" "))
        assertEquals(expected, lex(" \'\' "))
    }

    @Test
    fun testUnterminatedQuotes() {
        assertThrows<UnterminatedQuotesException> { lex("\'") }
        assertThrows<UnterminatedQuotesException> { lex("\"") }
        assertThrows<UnterminatedQuotesException> { lex("\'\'\'") }
        assertThrows<UnterminatedQuotesException> { lex("\"\"\"") }
    }

    @Test
    fun testEmptyQuotesInWord() {
        val expectedStrange = listOf(WORD(""), WORD("b"))

        assertEquals(expectedStrange, lex("\"\"b"))
        assertEquals(expectedStrange, lex("\'\'b"))

        val expectedUsual = listOf(WORD("ab"))

        assertEquals(expectedUsual, lex("a\"\"b"))
        assertEquals(expectedUsual, lex("a\'\'b"))
    }

    @Test
    fun testAssignSimple() {
        val expected = listOf(WORD("a"), ASSIGN, WORD("b"))
        assertEquals(expected, lex("a=b"))
    }

    @Test
    fun testAssignWithSpaces() {
        val expected = listOf(WORD("a"), SPACE, ASSIGN, SPACE, WORD("b"))
        assertEquals(expected, lex("a =   b"))
    }

    @Test
    fun testAssignMultiple() {
        val expected = listOf(ASSIGN, ASSIGN, SPACE, ASSIGN, WORD("a"), ASSIGN)
        assertEquals(expected, lex("== =a="))
    }

    @Test
    fun testAssignQuoted() {
        val expected = listOf(WORD("="))

        assertEquals(expected, lex("\'=\'"))
        assertEquals(expected, lex("\"=\""))
    }

    @Test
    fun testAssignNearEmptyQuotes() {
        val expectedStrange = listOf(WORD(""), ASSIGN, WORD(""))
        assertEquals(expectedStrange, lex("\"\"=\'\'"))
    }

}

class LexerTestPostprocess
