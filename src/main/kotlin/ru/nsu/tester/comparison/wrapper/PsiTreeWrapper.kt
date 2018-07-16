package ru.nsu.tester.comparison.wrapper

import ru.nsu.tester.comparison.deserialization.PsiRule
import ru.nsu.tester.comparison.deserialization.PsiToken

class PsiTreeWrapper(val tree: PsiRule) : TreeWrapper() {
    override val name = tree.name
    override val text = (tree as? PsiToken)?.text ?: tree.name
    override val textRange = tree.textRange
            .replace(Regex("(//.*?((\r?\n)|$))|((?s)/\\*.*?\\*/)"), "")
            .replace(Regex("[\r\n\t ]"), "")
            .replace(";", "")
    override val index: Int
        get() {
            if (tree.parent == null) return 0
            val parent = PsiTreeWrapper(tree.parent!!)
            if (parent.valuableChildrenCount == 1 && parent.tree.parent != null) return parent.index
            return parent.tree.children.indexOf(this.tree)
        }
    override val childrenCount = tree.children.size
    override val valuableChildrenCount = tree.children
                    .map { PsiTreeWrapper(it) }
                    .count { it.isValuable && !(it.name != "PsiToken" && it.childrenCount == 0) }
    override val isRedundant = false
    override val isValuable = !unvaluable.contains(text)

    override fun getChild(i: Int): TreeWrapper = PsiTreeWrapper(tree.children[i])

    override fun nextValuableChild(startChildNumber: Int): TreeWrapper? {
        val start = tree.getChild(startChildNumber) ?: return null
        val valuable = PsiTreeWrapper(start)
        if (valuable.isRedundant || valuable.valuableChildrenCount == 1)
            return valuable.nextValuableChild(0) ?: nextValuableChild(valuable.index + 1)
        if (start !is PsiToken
                && valuable.isValuable
                && valuable.childrenCount > 0
                && !valuable.name.toLowerCase().contains("annotation")
                && !valuable.name.toLowerCase().contains("label")) return valuable

        return nextValuableChild(valuable.index + 1)
    }
}