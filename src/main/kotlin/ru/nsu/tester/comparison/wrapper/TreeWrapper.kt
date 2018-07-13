package ru.nsu.tester.comparison.wrapper

internal val unvaluable: List<String> = listOf(
        "<EOF>",
        "emptylist",
        "\n", "\r\n",
        "Semis", "Semi", ";")

abstract class TreeWrapper {
    abstract val name: String
    abstract val text: String
    abstract val textRange: String
    abstract val index: Int
    abstract val childrenCount: Int
    abstract val valuableChildrenCount: Int
    abstract val isRedundant: Boolean
    abstract val isValuable: Boolean

    abstract fun getChild(i: Int) : TreeWrapper
    abstract fun nextValuableChild(startChildNumber: Int) : TreeWrapper?
}