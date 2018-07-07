package ru.nsu.tester.comparison.deserialization

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.children

val skipped = listOf(
        "WHITE_SPACE",
        "EOL_COMMENT",
        "BLOCK_COMMENT",
        "KDoc")

object PsiTreeBuilder {
    fun build(node: ASTNode) : PsiRule {
        val root = if (node.toString().startsWith("PsiElement") || node.toString().contains("empty list"))
            PsiToken(node.elementType.toString(), node.text, node.textRange) else
            PsiRule(node.elementType.toString(), node.textRange)
        node.children().forEach {
            if (!skipped.contains(it.elementType.toString())) {
                root.addChild(build(it))
            }
        }
        return root
    }
}