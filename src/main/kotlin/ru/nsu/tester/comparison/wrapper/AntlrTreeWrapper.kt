package ru.nsu.tester.comparison.wrapper

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

val redundant = listOf("Semis", "TopLevelObject", "Declaration", "SimpleIdentifier", "FunctionBody")

class AntlrTreeWrapper(val tree: ParseTree) : TreeWrapper {
    override fun isRedundant(): Boolean = redundant.contains(getName())

    override fun getName() = tree::class.simpleName?.removeSuffix("Context")

    override fun getIndex(): Int {
        val parent = AntlrTreeWrapper(tree.parent)
        if (parent.valuableChildrenCount() == 1) return parent.getIndex()
        var index: Int = -1
        for (i in 0 until parent.childrenCount()) {
            if (parent.tree.getChild(i) === tree) index = i
        }
        return index
    }

    override fun getText(): String? {
        return when (tree) {
            is TerminalNode -> tree.symbol.text
            else -> getName()
        }
    }

    override fun getChild(i: Int) = AntlrTreeWrapper(tree.getChild(i))

    override fun childrenCount() = tree.childCount

    override fun valuableChildrenCount(): Int {
        var count = 0
        for (i in 0 until tree.childCount) {
            val child = AntlrTreeWrapper(tree.getChild(i))
            if (child.isValuable() && !child.isRedundant()) count++
            if (child.isRedundant()) count += child.valuableChildrenCount()
        }

        return count
    }

    override fun nextValuableChild(startChildNumber: Int): TreeWrapper? {
        val start = tree.getChild(startChildNumber) ?: return null
        val valuable = AntlrTreeWrapper(start)
        if (valuable.isRedundant() || valuable.valuableChildrenCount() == 1)
            return valuable.nextValuableChild(0) ?: nextValuableChild(valuable.getIndex() + 1)
        if (start !is TerminalNode && valuable.isValuable()) return valuable

        return nextValuableChild(valuable.getIndex() + 1)
    }

}