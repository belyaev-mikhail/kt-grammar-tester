package ru.nsu.tester.comparison.wrapper

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

val redundant = listOf(
        "VariableDeclaration",
        "Statements",
        "ClassMemberDeclarations",
        "FunctionBody",
        "Parameter",
        "EnumEntries")

class AntlrTreeWrapper(val tree: ParseTree) : TreeWrapper() {
    override fun isRedundant(): Boolean = redundant.contains(getName())

    override fun getName() = tree::class.simpleName?.removeSuffix("Context") ?: " "

    // TODO: formalize logic
    override fun getIndex(): Int {
        val parent = AntlrTreeWrapper(tree.parent)
        if (parent.valuableChildrenCount() == 1) {
            return parent.getIndex()
        }
        var index: Int = -1
        var redundantPlus = 0
        for (i in 0 until parent.childrenCount()) {
            val childI = parent.tree.getChild(i)
            val wrappedChildI = AntlrTreeWrapper(childI)
            if (childI === tree) {
                index = i
                break
            }
            if (wrappedChildI.isRedundant()) redundantPlus += wrappedChildI.childrenCount()
        }
        if (parent.isRedundant()) {
            return index + redundantPlus + parent.getIndex()
        }
        return index + redundantPlus
    }

    override fun getText(): String {
        return when (tree) {
            is TerminalNode -> tree.symbol.text
            else -> getName() ?: " "
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

    // TODO: formalize logic
    override fun nextValuableChild(startChildNumber: Int): TreeWrapper? {
        var redundantChildrenPlus = 0
        for (i in 0 until startChildNumber) {
            if (i >= tree.childCount) break
            val next = AntlrTreeWrapper(tree.getChild(i))
            if (next.isRedundant() && next.childrenCount() >= startChildNumber - i + 1)
                return next.nextValuableChild(startChildNumber - i)
            if (next.isRedundant())
                redundantChildrenPlus += next.childrenCount() - 1
        }
        val start = tree.getChild(startChildNumber - redundantChildrenPlus) ?: return null
        val valuable = AntlrTreeWrapper(start)
        if (valuable.isRedundant() || valuable.valuableChildrenCount() == 1) {
            if (valuable.childrenCount() == 0) return null
            return valuable.nextValuableChild(0)
                    ?: nextValuableChild(startChildNumber + valuable.childrenCount())
        }
        if (start !is TerminalNode && valuable.isValuable())
            return valuable

        return nextValuableChild(startChildNumber + 1)
    }
}