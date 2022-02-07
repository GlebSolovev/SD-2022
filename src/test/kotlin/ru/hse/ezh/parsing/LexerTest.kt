package ru.hse.ezh.parsing

import org.junit.jupiter.api.assertThrows
import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.SpaceNearAssignException
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

class LexerTestPostprocess {

    private val emptyEnv = Environment()

    @Test
    fun testRemoveSpaces() {
        val input = listOf(SPACE, WORD("a"), SPACE, WORD("b"), SPACE)
        val expected = listOf(WORD("a"), WORD("b"))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testRemoveSingleSpace() {
        val input = listOf(SPACE)
        val expected = listOf<Token>()

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testSimpleMerge() {
        val input = listOf(WORD("a"), WORD("b"))
        val expected = listOf(WORD("ab"))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testMergeEmptyWords() {
        val input = listOf(SPACE, WORD(""), SPACE, WORD(""), WORD(""), SPACE)
        val expected = listOf(WORD(""), WORD(""))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testMergeEmptyWordAtEnd() {
        val input = listOf(WORD("a"), SPACE, WORD(""), WORD(""))
        val expected = listOf(WORD("a"), WORD(""))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testAssignSimple() {
        val input = listOf(WORD("a"), ASSIGN, WORD("b"))
        val expected = listOf(WORD("a"), ASSIGN, WORD("b"))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testAssignMultiple() {
        val input = listOf(ASSIGN, ASSIGN)
        val expected = listOf(ASSIGN, ASSIGN)

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testMergeNearAssign() {
        val input = listOf(WORD("a"), WORD("b"), ASSIGN, WORD("c"))
        val expected = listOf(WORD("ab"), ASSIGN, WORD("c"))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testMergeEmptyNearAssign() {
        val input = listOf(WORD(""), WORD(""), ASSIGN, WORD(""), WORD(""))
        val expected = listOf(WORD(""), ASSIGN, WORD(""))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testSpaceNearAssign() {
        val inputAfter = listOf(ASSIGN, SPACE)
        assertThrows<SpaceNearAssignException> { Lexer.postprocess(inputAfter, emptyEnv) }

        val inputBefore = listOf(SPACE, ASSIGN)
        assertThrows<SpaceNearAssignException> { Lexer.postprocess(inputBefore, emptyEnv) }
    }

    @Test
    fun testConsecutiveSpaces() {
        val input = listOf(WORD("a"), SPACE, SPACE, SPACE, WORD("b"))
        val expected = listOf(WORD("a"), WORD("b"))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

}
