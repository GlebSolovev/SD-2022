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
        val expectedFully = listOf(WORD("abc"))
        assertEquals(expectedFully, lex("\'a\'b\'c\'"))

        val expectedWeakly = listOf(WORD("abc"))
        assertEquals(expectedWeakly, lex("\"a\"b\"c\""))
    }

    @Test
    fun testQuotesAndSpaces() {
        val expectedFully = listOf(SPACE, WORD("a"), SPACE, WORD("b"), SPACE)
        assertEquals(expectedFully, lex(" \"a\" \"b\" "))

        val expectedWeakly = listOf(SPACE, WORD("a"), SPACE, WORD("b"), SPACE)
        assertEquals(expectedWeakly, lex(" \'a\' \'b\' "))
    }

    @Test
    fun testEmptyQuotes() {
        val expectedFully = listOf(SPACE, WORD(""), SPACE)
        assertEquals(expectedFully, lex(" \"\" "))

        val expectedWeakly = listOf(SPACE, WORD(""), SPACE)
        assertEquals(expectedWeakly, lex(" \'\' "))
    }

    @Test
    fun testUnterminatedQuotes() {
        assertThrows<UnterminatedQuotesException> { lex("\'") }
        assertThrows<UnterminatedQuotesException> { lex("\"") }
        assertThrows<UnterminatedQuotesException> { lex("\'\'\'") }
        assertThrows<UnterminatedQuotesException> { lex("\"\"\"") }
    }

}
