package ru.nsu.tester.comparison.wrapper

internal val unvaluable: List<String> = listOf(
        "<EOF>",
        "emptylist",
        "\n", "\r\n",
        "Semis", "Semi", ";")

abstract class TreeWrapper {
    override fun toString() : String = getName()

    fun isValuable(): Boolean = !unvaluable.contains(getText())

    abstract fun isRedundant(): Boolean

    abstract fun getName(): String

    abstract fun getIndex(): Int

    abstract fun getText(): String

    abstract fun getChild(i: Int): TreeWrapper

    abstract fun childrenCount(): Int

    abstract fun valuableChildrenCount(): Int

    abstract fun nextValuableChild(startChildNumber: Int): TreeWrapper?
}