package ru.hse.ezh

import main
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class HelloWorldTest {
    @Test
    fun testHelloWorld() {
        val testStream = ByteArrayOutputStream()
        System.setOut(PrintStream(testStream))

        main()

        Assertions.assertEquals("Hell word\n", testStream.toString().replace("\r\n", "\n"))
    }
}
