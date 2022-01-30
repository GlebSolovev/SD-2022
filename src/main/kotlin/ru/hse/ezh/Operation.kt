package ru.hse.ezh

import java.io.InputStream
import java.io.OutputStream

sealed class Operation

class Assignment(val rhs: WORD, val lhs: WORD) : Operation() {

    fun doAssign(env: Environment): Unit = TODO("Not yet implemented")

}

abstract class Command(val args: List<String>) : Operation() {

    abstract fun execute(input: InputStream, out: OutputStream, err: OutputStream, env: Environment): Int

}

class Exit(val statusCode: Int) : Operation()
