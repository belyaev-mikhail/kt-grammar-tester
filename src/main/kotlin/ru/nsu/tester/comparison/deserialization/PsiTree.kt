package ru.nsu.tester.comparison.deserialization

open class PsiRule(val name: String) {
    lateinit var parent: PsiRule
    var children = mutableListOf<PsiRule>()

    fun getChild(i: Int) = children.getOrNull(i)

    fun addChild(child: PsiRule) {
        children.add(child)
        child.parent = this
    }
}

class PsiToken(val type: String, val text: String) : PsiRule("PsiToken")