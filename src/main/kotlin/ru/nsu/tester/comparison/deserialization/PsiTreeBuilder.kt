package ru.nsu.tester.comparison.deserialization

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.children

object PsiTreeBuilder {
    private const val INDENT = "  "

    private fun String.parseLine() : PsiRule {
        return when {
            this.startsWith("PsiElement") -> {
                val type = Regex("\\((.+?)\\)").find(this)?.groupValues?.get(1) ?: " "
                val token = Regex("\\('(.+?)'\\)").find(this)?.groupValues?.get(1) ?: " "
                PsiToken(type, token)
            }
            this.startsWith("Element") -> {
                val rule = Regex("\\((.+?)\\)").find(this)?.groupValues?.get(1) ?: " "
                PsiRule(rule)
            }
            this.contains("empty list") -> PsiToken("", this.replace(Regex("[\\s+<>]"), ""))
            else -> PsiRule(this.replace(Regex("[\\s+0-9,()]"), ""))
        }
    }

    fun build(lines: List<String>) : PsiRule {
        val root = PsiRule("KtFile")
        var currNode = root
        currNode.parent = currNode

        var prevIndent = 0
        for (line in lines.subList(1, lines.lastIndex + 1)) {
            val currIndent = (line.length - line.replace(INDENT, "").length) / INDENT.length
            val element = line.parseLine()

            for (i in currIndent - prevIndent..0) currNode = currNode.parent
            currNode.addChild(element)
            currNode = element
            prevIndent = currIndent
        }

        return root
    }

    fun build(psi: ASTNode) : PsiRule {
        val root = (psi.toString() + "('" + psi.psi.text + "')").parseLine()
        psi.children().forEach {
            if (it.elementType.toString() != "WHITE_SPACE"
                    && it.elementType.toString() != "EOL_COMMENT"
                    && it.elementType.toString() != "BLOCK_COMMENT"
                    && it.elementType.toString() != "KDoc") {
                root.addChild(build(it))
            }
        }
        return root
    }
}