package ru.hse.ezh

sealed class Token

data class WORD(val str: String) : Token()

data class SUBST(val varName: String) : Token()

data class QSUBST(val varName: String) : Token()

object SPACE : Token()

object ASSIGN : Token()

object PIPE : Token()
