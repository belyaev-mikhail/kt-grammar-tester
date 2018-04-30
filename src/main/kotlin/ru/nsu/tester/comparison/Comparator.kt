package ru.nsu.tester.comparison

import ru.nsu.gen.KotlinParser.KotlinFileContext
import ru.nsu.tester.comparison.deserialization.PsiTreeBuilder
import ru.nsu.tester.comparison.wrapper.AntlrTreeWrapper
import ru.nsu.tester.comparison.wrapper.PsiTreeWrapper
import ru.nsu.tester.comparison.wrapper.TreeWrapper
import java.io.File

data class ComparisonError(val antlrRule: String?, val psiRule: String?) {
    override fun toString(): String {
        return "Difference found in ANTLR: $antlrRule / PSI: $psiRule"
    }
}

data class ComparisonResult(val errors: List<ComparisonError>?) {
    fun isCorrect() = errors == null || errors.isEmpty()
    fun output() {
        if (isCorrect()) println("Compared successfully")
        else {
            println("Comparison errors detected:")
            errors!!.forEach { println(it) }
        }
    }
}

object Comparator {
    fun inspectTree(path: String, antlrTree: KotlinFileContext): ComparisonResult {
        val psi = File(path.replace(".kt", ".txt"))
        if (!psi.exists()) return ComparisonResult(null)
        val psiTree = PsiTreeBuilder.build(psi.readLines())
        val errors = mutableListOf<ComparisonError>()
        compareTrees(AntlrTreeWrapper(antlrTree), PsiTreeWrapper(psiTree), errors)

        return ComparisonResult(errors)
    }

    private fun compareTrees(antlrNode: TreeWrapper, psiNode: TreeWrapper, errors: MutableList<ComparisonError>) {
        try {
            if (antlrNode.valuableChildrenCount() != psiNode.valuableChildrenCount()) throw Exception()
            var antlrNextToCheck = antlrNode.nextValuableChild(0)
            var psiNextToCheck = psiNode.nextValuableChild(0)
            while (!(antlrNextToCheck == null && psiNextToCheck == null)) {
                if (antlrNextToCheck == null || psiNextToCheck == null) throw Exception()

                compareTrees(antlrNextToCheck, psiNextToCheck, errors)

                antlrNextToCheck = antlrNode.nextValuableChild(antlrNextToCheck.getIndex() + 1);
                psiNextToCheck = psiNode.nextValuableChild(psiNextToCheck.getIndex() + 1);
            }
        } catch (ex: Exception) {
            errors.add(ComparisonError(antlrNode.getName(), psiNode.getName()))
        }
    }
}