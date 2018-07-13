package ru.nsu.tester.comparison.deserialization

open class PsiRule(val name: String, var textRange: String) {
    var parent: PsiRule? = null
    var children = mutableListOf<PsiRule>()

    fun getChild(i: Int) = children.getOrNull(i)

    fun addChild(child: PsiRule?, i: Int = -1) {
        if (child != null) {
            if (i >= 0) children.add(i, child)
            else children.add(child)
            child.parent = this
        }
    }

    fun removeChild(i: Int) {
        if (i in 0 until children.count()) {
            children.removeAt(i)
        }
    }

    fun isLeaf() : Boolean {
        if (children.count() == 0) return true
        if (children.count() == 1) return getChild(0)!!.isLeaf()
        return false
    }
}

class PsiToken(val type: String, val text: String, textRange: String) : PsiRule("PsiToken", textRange)