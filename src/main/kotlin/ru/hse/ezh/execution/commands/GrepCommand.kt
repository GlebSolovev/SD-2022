package ru.hse.ezh.execution.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import ru.hse.ezh.Environment
import ru.hse.ezh.exceptions.ExecutionIOException
import ru.hse.ezh.execution.Command
import ru.hse.ezh.execution.commands.utils.CHARSET
import ru.hse.ezh.execution.commands.utils.readAllWrapped
import ru.hse.ezh.execution.commands.utils.writeLineWrapped

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.regex.PatternSyntaxException

/**
 * This class represents the 'grep' command.
 *
 * Finds the strings matching the provided regex in a file and prints them to output.
 *
 * @constructor
 * @param args Command arguments:
 *  - if "-w" flag is provided, only entire words (delimited by \s+) are matched
 *  - if "-i" flag is provided, case-insensitive mode is enabled
 *  - if "-A <u>NUM</u>" argument is provided, for every match the following <u>NUM</u> lines are printed
 *  - pattern, required
 *  - filename, optional
 */
class GrepCommand(args: List<String>) : Command(args) {

    /**
     * Executes the command (see [Command.execute]).
     *
     * If a filename is given, reads its contents as input. Else reads from [input] instead of file.
     *
     * Finds the matches of the pattern in input.
     * Then prints the lines containing the matches to [out] delimited by \n.
     *
     * If "-A" flag is provided:
     *  - overlapping and consecutive regions are merged
     *  - when printing, regions are delimited by --\n
     *
     * Does not support huge files (> 2 GB).
     *
     * Does not support multiline patterns.
     *
     * @param input Stream to read input from in case no filename is given.
     * @param out Stream to print output to.
     * @param err Stream to print errors to.
     * @param env Ignored.
     *
     * @return
     * - 0 on success
     * - 1 if argument list is invalid
     * - 2 if an [java.io.IOException] happens during reading file
     * - 3 if pattern is invalid
     *
     * @throws ExecutionIOException If [input] or [out] stream error occurred.
     */
    @Throws(ExecutionIOException::class)
    override fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int {
        val cli = CLI()

        try {
            cli.parse(args)
        } catch (e: CliktError) {
            err.writeLineWrapped("grep: invalid arguments: ${e.message}")
            return ReturnCode.INVALID_ARGS.code
        }

        val regex = try {
            cli.pattern.toRegex(
                if (cli.caseInsensitiveMode) setOf(RegexOption.IGNORE_CASE) else emptySet()
            )
        } catch (e: PatternSyntaxException) {
            err.writeLineWrapped("grep: invalid pattern: ${e.message}")
            return ReturnCode.INVALID_PATTERN.code
        }

        val contentLines = try {
            cli.file?.readText(CHARSET) ?: input.readAllWrapped()
        } catch (e: IOException) {
            err.writeLineWrapped("grep: IOException during reading file\n${e.message}")
            return ReturnCode.IO_EXCEPTION.code
        }.lines()

        val matchingLineIndices = contentLines.withIndex()
            .asSequence()
            .filter {
                val line = it.value
                if (cli.entireWordsMode) {
                    line.split("\\s+".toRegex()).any { word -> word.matches(regex) }
                } else {
                    line.contains(regex)
                }
            }
            .map { it.index }

        var lastPrintedLine = 0
        matchingLineIndices.forEach { matchingLineIndex ->
            val upperBound = min(matchingLineIndex + (cli.extraLinesCount ?: 0), contentLines.size)
            val range = max(lastPrintedLine, matchingLineIndex)..upperBound
            if (lastPrintedLine + 1 !in range && cli.extraLinesCount != null) {
                out.writeLineWrapped("--")
            }
            range.forEach {
                out.writeLineWrapped(contentLines[it])
            }
            lastPrintedLine = upperBound
        }

        return ReturnCode.SUCCESS.code
    }

    private class CLI : CliktCommand() {

        val entireWordsMode: Boolean by option("-w", help = "match only entire words")
            .flag(default = false)
        val caseInsensitiveMode: Boolean by option("-i", help = "enable case-insensitive mode")
            .flag(default = false)
        val extraLinesCount: Int? by option("-A", help = "print extra following lines for each match")
            .int()
            .validate { it >= 0 }

        val pattern: String by argument()
        val file: File? by argument().file(mustExist = true, canBeDir = false, mustBeReadable = true).optional()

        override fun run() {
            // do nothing
        }
    }

    private enum class ReturnCode(val code: Int) {
        SUCCESS(0),
        INVALID_ARGS(1),
        IO_EXCEPTION(2),
        INVALID_PATTERN(3),
    }

}
