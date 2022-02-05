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
    }

    @Test
    fun testPutAndGetVariable() {
        val env = Environment()
        assertEquals("", env.getVariable("var"))

        env.putVariable("var", "value")
        assertEquals("value", env.getVariable("var"))

        env.putVariable("var", "new value")
        assertEquals("new value", env.getVariable("var"))
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
        assertThrows<IllegalStateException> { env.putVariable("var", "new value") }
        assertThrows<IllegalStateException> { env.putVariable("new var", "new value") }
    }

}
