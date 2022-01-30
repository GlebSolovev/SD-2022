package ru.hse.ezh

import ru.hse.ezh.commands.CatCommand
import ru.hse.ezh.commands.EchoCommand
import ru.hse.ezh.commands.PwdCommand
import ru.hse.ezh.commands.WcCommand

object Parser {

    val keywords: Map<Token, (List<String>) -> Operation> = mapOf(
        WORD("cat") to ::CatCommand,
        WORD("echo") to ::EchoCommand,
        WORD("pwd") to ::PwdCommand,
        WORD("wc") to ::WcCommand,
    )

    fun parse(tokens: List<Token>): List<Operation> = TODO("Not yet implemented")

}
