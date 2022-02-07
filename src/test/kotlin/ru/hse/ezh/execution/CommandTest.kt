package ru.hse.ezh.execution

import ru.hse.ezh.execution.commands.CatCommand
import ru.hse.ezh.execution.commands.EchoCommand

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CommandTest {

    @Test
    fun testEchoEquals() {
        assertEquals(EchoCommand(listOf("a", "b")), EchoCommand(listOf("a", "b")))
        assertEquals(EchoCommand(listOf("a", "b")).hashCode(), EchoCommand(listOf("a", "b")).hashCode())
    }

    @Test
    fun testEchoNotEquals() {
        assertNotEquals(EchoCommand(listOf("a", "b")), EchoCommand(listOf("a", "not b")))
        assertNotEquals(EchoCommand(listOf("a", "b")).hashCode(), EchoCommand(listOf("a", "not b")).hashCode())
    }

    @Test
    fun testNotEchoNotEquals() {
        assertNotEquals(EchoCommand(listOf("a")) as Command, CatCommand(listOf("a")) as Command)
        assertNotEquals(EchoCommand(listOf("a")).hashCode(), CatCommand(listOf("a")).hashCode())
    }

}
