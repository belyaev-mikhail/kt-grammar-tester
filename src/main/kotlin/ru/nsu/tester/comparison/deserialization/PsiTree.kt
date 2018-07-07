package ru.nsu.tester.comparison.deserialization

import com.intellij.openapi.util.TextRange

open class PsiRule(val name: String, textRange: TextRange) {
    lateinit var parent: PsiRule
    var children = mutableListOf<PsiRule>()
    val textRange = textRange

    fun getChild(i: Int) = children.getOrNull(i)

    fun addChild(child: PsiRule) {
        children.add(child)
        child.parent = this
    }
}

class PsiToken(val type: String, val text: String, textRange: TextRange) : PsiRule("PsiToken", textRange)