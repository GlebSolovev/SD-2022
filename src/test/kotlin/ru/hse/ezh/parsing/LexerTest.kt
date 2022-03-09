package ru.hse.ezh.parsing

import org.junit.jupiter.api.assertThrows
import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.EmptySubstitutionException
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

    @Test
    fun testUnquotedOneSidedPipe() {
        val expected = listOf(WORD("pwd"), PIPE)
        assertEquals(expected, lex("pwd|"))
    }

    @Test
    fun testUnquotedTwoSidedPipe() {
        val expected = listOf(WORD("pwd"), PIPE, WORD("wc"))
        assertEquals(expected, lex("pwd|wc"))
    }

    @Test
    fun testUnquotedPipeWithSpaces() {
        val expected = listOf(WORD("pwd"), SPACE, PIPE, SPACE, WORD("cat"))
        assertEquals(expected, lex("pwd  | cat"))
    }

    @Test
    fun testSingleSubst() {
        val expected = listOf(SUBST("var"))
        assertEquals(expected, lex("\$var"))
    }

    @Test
    fun testSubstBetweenWordsWithSpaces() {
        val expected = listOf(WORD("ex"), SPACE, SUBST("var"), SPACE, WORD("t"))
        assertEquals(expected, lex("ex \$var t"))
    }

    @Test
    fun testConsecutiveSubsts() {
        val expected = listOf(SUBST("x"), SUBST("y"))
        assertEquals(expected, lex("\$x\$y"))
    }

    @Test
    fun testSubstBeforeQuotes() {
        val expectedFully = listOf(SUBST("x"), WORD("word"))
        assertEquals(expectedFully, lex("\$x\'word\'"))

        val expectedWeakly = listOf(SUBST("x"), WORD("word"))
        assertEquals(expectedWeakly, lex("\$x\"word\""))
    }

    @Test
    fun testSubstWithAssign() {
        val expected = listOf(SUBST("x"), ASSIGN, SUBST("y"))
        assertEquals(expected, lex("\$x=\$y"))
    }

    @Test
    fun testSubstWithPipe() {
        val expected = listOf(SUBST("x"), PIPE, SUBST("y"))
        assertEquals(expected, lex("\$x|\$y"))
    }

    @Test
    fun testEmptySubst() {
        assertThrows<EmptySubstitutionException> { lex("\$\$") }
        assertThrows<EmptySubstitutionException> { lex("\$ ") }
        assertThrows<EmptySubstitutionException> { lex("\$=") }
        assertThrows<EmptySubstitutionException> { lex("\$|") }
        assertThrows<EmptySubstitutionException> { lex("\$\'") }
        assertThrows<EmptySubstitutionException> { lex("\$\"") }
    }

    @Test
    fun testFullyQuotedSubst() {
        val expected = listOf(WORD("\$x"))
        assertEquals(expected, lex("\'\$x\'"))
    }

    @Test
    fun testQuotedPipe() {
        val expectedFully = listOf(WORD("|"))
        assertEquals(expectedFully, lex("\'|\'"))

        val expectedWeakly = listOf(WORD("|"))
        assertEquals(expectedWeakly, lex("\"|\""))
    }

    @Test
    fun testSingleQSubst() {
        val expected = listOf(QSUBST("x"))
        assertEquals(expected, lex("\"\$x\""))
    }

    @Test
    fun testQSubstWithWords() {
        val expected = listOf(WORD("word"), QSUBST("x"), WORD("  word"))
        assertEquals(expected, lex("\"word\$x  word\""))
    }

    @Test
    fun testQSubstBetweenUnquotedWord() {
        val expected = listOf(WORD("word"), QSUBST("x"), WORD("word"))
        assertEquals(expected, lex("word\"\$x\"word"))
    }

    @Test
    fun testMultipleQSubsts() {
        val expected = listOf(QSUBST("x"), QSUBST("y"))
        assertEquals(expected, lex("\"\$x\$y\""))
    }

    @Test
    fun testQSubstsWithSpecialCharacters() {
        val expected = listOf(QSUBST("x"), WORD("|"), QSUBST("y"), WORD("="), QSUBST("z"), WORD("\'"))
        assertEquals(expected, lex("\"\$x|\$y=\$z\'\""))
    }

    @Test
    fun testUnterminatedQuotesAfterQSubst() {
        assertThrows<UnterminatedQuotesException> { lex("\"\$x") }
    }

    @Test
    fun testEmptyQSubst() {
        assertThrows<EmptySubstitutionException> { lex("\"\$\"") }
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

    @Test
    fun testReplaceQSubst() {
        val env = Environment()
        env.putVariable("x", "value of x")

        val input = listOf(QSUBST("x"))
        val expected = listOf(WORD("value of x"))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testReplaceSubst() {
        val env = Environment()
        env.putVariable("x", "value of x")

        val input = listOf(SUBST("x"))
        val expected = listOf(WORD("value"), WORD("of"), WORD("x"))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testEmptyReplace() {
        val env = Environment()
        env.putVariable("x", "")

        val input = listOf(SUBST("x"), SPACE, QSUBST("x"))
        val expected = listOf(WORD(""))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testReplaceBySpaces() {
        val env = Environment()
        env.putVariable("x", "   ")

        val input = listOf(SUBST("x"), QSUBST("x"))
        val expected = listOf(WORD("   "))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testReplaceBySpacesAtEnds() {
        val env = Environment()
        env.putVariable("x", "  value  ")

        val input = listOf(SUBST("x"), QSUBST("x"))
        val expected = listOf(WORD("value"), WORD("  value  "))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testReplaceBySpaceCharacters() {
        val env = Environment()
        env.putVariable("x", "\tvalue\r\nof\t\t\nx\r")

        val input = listOf(SUBST("x"), QSUBST("x"))
        val expected = listOf(WORD("value"), WORD("of"), WORD("x"), WORD("\tvalue\r\nof\t\t\nx\r"))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testReplaceBySpecialCharacters() {
        val env = Environment()
        env.putVariable("x", "value=|\$x\'\$x\'\"\$x\"")

        val input = listOf(SUBST("x"), SPACE, QSUBST("x"))
        val expected = listOf(WORD("value=|\$x\'\$x\'\"\$x\""), WORD("value=|\$x\'\$x\'\"\$x\""))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testStrangeReplace() {
        val env = Environment()
        env.putVariable("x", "\t\r\nё\n|\u9637= \'\'")

        val input = listOf(SUBST("x"), SPACE, QSUBST("x"))
        val expected = listOf(WORD("ё"), WORD("|\u9637="), WORD("\'\'"), WORD("\t\r\nё\n|\u9637= \'\'"))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testReplaceUnknownVariable() {
        val input = listOf(SUBST("x"), SPACE, QSUBST("x"))
        val expected = listOf(WORD(""))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testMergeReplaced() {
        val env = Environment()
        env.putVariable("x", "ex")
        env.putVariable("y", "it")
        env.putVariable("z", " 5")

        val input = listOf(SUBST("x"), QSUBST("y"), SPACE, SUBST("z"))
        val expected = listOf(WORD("exit"), WORD("5"))

        assertEquals(expected, Lexer.postprocess(input, env))
    }

    @Test
    fun testPipeSimple() {
        val input = listOf(WORD("pwd"), PIPE, WORD("wc"))
        val expected = listOf(WORD("pwd"), PIPE, WORD("wc"))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }

    @Test
    fun testPipeMultipleWithSpaces() {
        val input = listOf(WORD("word"), SPACE, PIPE, SPACE, PIPE, PIPE, SPACE, WORD("word"))
        val expected = listOf(WORD("word"), PIPE, PIPE, PIPE, WORD("word"))

        assertEquals(expected, Lexer.postprocess(input, emptyEnv))
    }
}
