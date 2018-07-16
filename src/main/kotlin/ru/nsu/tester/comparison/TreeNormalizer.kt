package ru.nsu.tester.comparison

import ru.nsu.tester.comparison.deserialization.PsiRule
import ru.nsu.tester.comparison.deserialization.PsiToken
import ru.nsu.tester.comparison.wrapper.PsiTreeWrapper
import ru.nsu.tester.comparison.wrapper.TreeWrapper

fun TreeWrapper.isValuableTerminal() : Boolean {
    return (name == "PsiToken" || name.contains("Operator") || name.contains("OPERATION")) && isValuable
}

fun TreeWrapper.isInSpecialChomskyForm() : Boolean {
    if (childrenCount <= 3) {

        /* If there are 3 children
         * (1) the middle one should be a valuable terminal
         * (2) the first and the last should be valuable terminals
         */
        if (childrenCount == 3
                && !getChild(1).isValuableTerminal()
                && !(getChild(0).isValuableTerminal() && getChild(2).isValuableTerminal()))
            return false

        return (0 until childrenCount).all { getChild(it).isInSpecialChomskyForm() }
    }
    return false
}

object TreeNormalizer {
    private fun PsiRule.normalize() {
        val childrenCount = children.count()

        if (childrenCount >= 3
                && !(childrenCount == 3 && children.first().isLeaf() && children.last().isLeaf())
                && !(childrenCount == 3 && PsiTreeWrapper(children[1]).isValuableTerminal())) {

            val newNonTerminalTextRange = this.textRange
                    .removeSuffix(getChild(childrenCount - 1)?.textRange ?: "")
                    .removeSuffix(getChild(childrenCount - 2)?.textRange ?: "")
            val newNonTerminal = PsiRule("newNonTerminal", newNonTerminalTextRange)
            (0 until childrenCount - 2).forEach {
                newNonTerminal.addChild(getChild(0))
                removeChild(0)
            }

            val middleChild = getChild(0)
            if (middleChild != null && !(PsiTreeWrapper(middleChild).isValuableTerminal())) {
                newNonTerminal.textRange += middleChild.textRange
                newNonTerminal.addChild(middleChild)
                removeChild(0)
            }
            this.addChild(newNonTerminal, 0)
        }

        children.forEach { it.normalize() }
    }

    private fun TreeWrapper.makeCopy() : PsiRule {
        if (childrenCount == 0) return PsiToken(name, text, textRange)
        val copy = PsiRule(name, textRange)
        (0 until childrenCount).forEach {
            var childI = getChild(it)
            // bad
            while (childI.isRedundant && childI.childrenCount == 1) childI = childI.getChild(0)
            if (childI.isRedundant) (0 until childI.childrenCount).forEach { copy.addChild(childI.getChild(it).makeCopy()) }
            else copy.addChild(childI.makeCopy())
        }
        return copy
    }

    fun normalize(tree: TreeWrapper) : TreeWrapper {
        if (tree.isInSpecialChomskyForm()) return tree
        val copy = tree.makeCopy()
        copy.normalize()
        return PsiTreeWrapper(copy)
    }
}