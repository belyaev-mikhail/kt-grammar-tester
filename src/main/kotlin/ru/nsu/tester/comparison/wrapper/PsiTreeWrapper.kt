package ru.nsu.tester.comparison.wrapper

import ru.nsu.tester.comparison.deserialization.PsiRule
import ru.nsu.tester.comparison.deserialization.PsiToken

class PsiTreeWrapper(val tree: PsiRule) : TreeWrapper() {
    override fun isRedundant(): Boolean = false

    override fun getName() = tree.name

    override fun getIndex(): Int {
        val parent = PsiTreeWrapper(tree.parent)
        if (parent.valuableChildrenCount() == 1) return parent.getIndex()
        return parent.tree.children.indexOf(this.tree)
    }

    override fun getText(): String = (tree as? PsiToken)?.text ?: tree.name

    override fun getChild(i: Int): TreeWrapper = PsiTreeWrapper(tree.children[i])

    override fun childrenCount() = tree.children.size

    override fun valuableChildrenCount(): Int {
        return if (!tree.children.isEmpty())
            tree.children.asSequence()
                    .filter { it -> PsiTreeWrapper(it).isValuable() }
                    .count()
        else 0
    }

    override fun nextValuableChild(startChildNumber: Int): TreeWrapper? {
        val start = tree.getChild(startChildNumber) ?: return null
        val valuable = PsiTreeWrapper(start)
        if (valuable.isRedundant() || valuable.valuableChildrenCount() == 1)
            return valuable.nextValuableChild(0) ?: nextValuableChild(valuable.getIndex() + 1)
        if (start !is PsiToken && valuable.isValuable()) return valuable

        return nextValuableChild(valuable.getIndex() + 1)
    }

}