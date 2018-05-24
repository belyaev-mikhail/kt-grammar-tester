package ru.nsu.tester.comparison.wrapper

internal val unvaluable: List<String> = listOf("\n", "\r\n", ";", "<EOF>", "emptylist")

// TODO: use properties access syntax
interface TreeWrapper {
    fun isValuable(): Boolean = !unvaluable.contains(getText())

    fun isRedundant(): Boolean

    fun getName(): String

    fun getIndex(): Int

    fun getText(): String

    fun getChild(i: Int): TreeWrapper

    fun childrenCount(): Int

    fun valuableChildrenCount(): Int

    fun nextValuableChild(startChildNumber: Int): TreeWrapper?
}