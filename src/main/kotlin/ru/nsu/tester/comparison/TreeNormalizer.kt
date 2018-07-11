package ru.nsu.tester.comparison

import ru.nsu.tester.comparison.deserialization.PsiRule
import ru.nsu.tester.comparison.deserialization.PsiToken
import ru.nsu.tester.comparison.wrapper.PsiTreeWrapper
import ru.nsu.tester.comparison.wrapper.TreeWrapper

object TreeNormalizer {
    private fun PsiRule.normalize() {
        val childrenCount = children.count()
        if (childrenCount <= 3) return

        val newNonTerminalTextRange = this.textRange
                .removeSuffix(getChild(childrenCount - 1)?.textRange ?: "")
                .removeSuffix(getChild(childrenCount - 2)?.textRange ?: "")
        val newNonTerminal = PsiRule("newNonTerminal", newNonTerminalTextRange)
        for (i in 0 until childrenCount - 2) {
            newNonTerminal.addChild(getChild(0))
            removeChild(0)
        }
        this.addChild(newNonTerminal, 0)

        for (child in children) {
            child.normalize()
        }
    }

    private fun TreeWrapper.makeCopy() : PsiRule {
        if (childrenCount == 0) return PsiToken(name, text, textRange)
        val copy = PsiRule(name, textRange)
        for (i in 0 until childrenCount) {
            copy.addChild(getChild(i).makeCopy())
        }
        return copy
    }

    fun normalize(tree: TreeWrapper) : TreeWrapper {
        val copy = tree.makeCopy()
        copy.normalize()
        return PsiTreeWrapper(copy)
    }
}