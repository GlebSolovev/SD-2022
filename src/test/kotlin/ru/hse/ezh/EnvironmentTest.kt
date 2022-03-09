package ru.hse.ezh

import org.junit.jupiter.api.assertThrows

import kotlin.test.Test
import kotlin.test.assertEquals

class EnvironmentTest {

    @Test
    fun testSimple() {
        val env = Environment()
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)
        assertEquals("", env.getVariable("var"))
        assertEquals(mapOf(), env.getAllVariables())
    }

    @Test
    fun testPutAndGetVariable() {
        val env = Environment()
        assertEquals("", env.getVariable("var"))

        env.putVariable("var", "value")
        assertEquals("value", env.getVariable("var"))
        assertEquals(mapOf("var" to "value"), env.getAllVariables())

        env.putVariable("var", "new value")
        assertEquals("new value", env.getVariable("var"))
        assertEquals(mapOf("var" to "new value"), env.getAllVariables())
    }

    @Test
    fun testGetAndSetExitStatus() {
        val env = Environment()
        assertEquals(Environment.ExitStatus.RUNNING, env.exitStatus)

        env.exitStatus = Environment.ExitStatus.EXITING
        assertEquals(Environment.ExitStatus.EXITING, env.exitStatus)

        assertThrows<IllegalStateException> { env.exitStatus = Environment.ExitStatus.RUNNING }
        assertThrows<IllegalStateException> { env.exitStatus = Environment.ExitStatus.EXITING }
    }

    @Test
    fun testPutAndGetVariableExitingStatus() {
        val env = Environment()
        env.putVariable("var", "value")
        env.exitStatus = Environment.ExitStatus.EXITING

        assertThrows<IllegalStateException> { env.getVariable("var") }
        assertThrows<IllegalStateException> { env.getAllVariables() }
        assertThrows<IllegalStateException> { env.putVariable("var", "new value") }
        assertThrows<IllegalStateException> { env.putVariable("new var", "new value") }
    }

    @Test
    fun testReplaceWith() {
        val env = Environment()
        env.putVariable("var", "value")

        val replaced = Environment()
        replaced.putVariable("old var", "old value")

        assertEquals(replaced, replaced.replaceWith(env))
        assertEquals(mapOf("var" to "value"), replaced.getAllVariables())

        env.putVariable("new var", "new value")
        assertEquals("", replaced.getVariable("new value"))

        assertEquals(Environment.ExitStatus.RUNNING, replaced.exitStatus)
        env.exitStatus = Environment.ExitStatus.EXITING
        assertEquals(Environment.ExitStatus.RUNNING, replaced.exitStatus)
    }

    @Test
    fun testReplaceWithExitStatus() {
        val env = Environment()
        env.exitStatus = Environment.ExitStatus.EXITING

        assertThrows<IllegalStateException> { env.replaceWith(Environment()) }
    }

}
