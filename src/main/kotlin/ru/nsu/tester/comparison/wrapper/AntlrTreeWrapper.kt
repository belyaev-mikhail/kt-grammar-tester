package ru.nsu.tester.comparison.wrapper

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

val redundant = listOf(
        "VariableDeclaration",
        "Statements",
        "ClassMemberDeclarations",
        "FunctionBody",
        "Parameter",
        "EnumEntries",
        "PostfixUnarySuffix",
        "NavigationSuffix",
        "UnescapedAnnotation")

class AntlrTreeWrapper(val tree: ParseTree) : TreeWrapper() {
    override val name = tree::class.simpleName?.removeSuffix("Context") ?: " "
    override val text =
            when (tree) {
                is TerminalNode -> tree.symbol.text
                else -> name
            }
    override val textRange = tree.text
            .replace(Regex("(//.*?((\r?\n)|$))|((?s)/\\*.*?\\*/)"), "")
            .replace(Regex("[\r\n ]"), "")
            .replace("<EOF>", "")
            .replace(";", "")
    override val index: Int
        get() {
            val parent = AntlrTreeWrapper(tree.parent)
            if (parent.valuableChildrenCount == 1 && parent.tree.parent != null) {
                return parent.index
            }
            var index: Int = -1
            var redundantPlus = 0
            for (i in 0 until parent.childrenCount) {
                val childI = parent.tree.getChild(i)
                val wrappedChildI = AntlrTreeWrapper(childI)
                if (childI === tree) {
                    index = i
                    break
                }
                if (wrappedChildI.isRedundant) redundantPlus += wrappedChildI.childrenCount
            }
            if (parent.isRedundant) {
                return index + redundantPlus + parent.index
            }
            return index + redundantPlus
        }
    override val childrenCount = tree.childCount
    override val valuableChildrenCount: Int
        get() {
            var count = 0
            for (i in 0 until tree.childCount) {
                val child = AntlrTreeWrapper(tree.getChild(i))
                if (child.textRange == "") continue
                if (child.isValuable && !child.isRedundant) count++
                if (child.isRedundant) count += child.valuableChildrenCount
            }

            return count
        }
    override val isRedundant = redundant.contains(name)
    override val isValuable = !unvaluable.contains(text)

    override fun getChild(i: Int): TreeWrapper = AntlrTreeWrapper(tree.getChild(i))

    override fun nextValuableChild(startChildNumber: Int): TreeWrapper? {
        var redundantChildrenPlus = 0
        for (i in 0 until startChildNumber) {
            if (i >= tree.childCount) break
            val next = AntlrTreeWrapper(tree.getChild(i))
            if (next.isRedundant && next.childrenCount >= startChildNumber - i + 1)
                return next.nextValuableChild(startChildNumber - i)
            if (next.isRedundant)
                redundantChildrenPlus += next.childrenCount - 1
        }
        val start = tree.getChild(startChildNumber - redundantChildrenPlus) ?: return null
        val valuable = AntlrTreeWrapper(start)
        if (valuable.isRedundant || valuable.valuableChildrenCount == 1) {
            if (valuable.childrenCount == 0) return null
            return valuable.nextValuableChild(0)
                    ?: nextValuableChild(startChildNumber + valuable.childrenCount)
        }
        if (start !is TerminalNode
                && valuable.isValuable
                && valuable.childrenCount > 0) return valuable

        return nextValuableChild(startChildNumber + 1)
    }
}