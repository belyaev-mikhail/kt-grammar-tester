package ru.nsu.tester.comparison

import org.jetbrains.kootstrap.FooBarCompiler
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.addKotlinSourceRoot
import org.jetbrains.kotlin.psi.KtPsiFactory
import ru.nsu.gen.KotlinParser.KotlinFileContext
import ru.nsu.tester.comparison.deserialization.PsiTreeBuilder
import ru.nsu.tester.comparison.wrapper.*

val considerSimilar = listOf(
        Pair("PostfixUnaryExpression", "DOT_QUALIFIED_EXPRESSION"),
        Pair("Identifier", "DOT_QUALIFIED_EXPRESSION"),
        Pair("MultiLineStringLiteral", "STRING_TEMPLATE"),
        Pair("LineStringLiteral", "STRING_TEMPLATE"),
        Pair("UserType", "USER_TYPE"))

data class ComparisonError(
        val antlrRule: String?,
        val psiRule: String?,
        val antlrTree: TreeWrapper,
        val psiTree: TreeWrapper) {

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
        val cfg = CompilerConfiguration()
        cfg.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        cfg.addKotlinSourceRoot(path)

        val ktFile = FooBarCompiler.setupMyEnv(cfg).getSourceFiles().first()
        val parsed = KtPsiFactory(ktFile).createFile(ktFile.virtualFile.path, ktFile.text)

        println("is valid: ${parsed.treeElement!!.textRange}")

        // TODO: assert psi doesn't have errors
        val psiTree = PsiTreeBuilder.build(parsed.treeElement!!)
        val errors = mutableListOf<ComparisonError>()
        compareTrees(AntlrTreeWrapper(antlrTree), PsiTreeWrapper(psiTree), errors)

        return ComparisonResult(errors)
    }

    private fun compareTrees(antlrNode: TreeWrapper, psiNode: TreeWrapper, errors: MutableList<ComparisonError>) {
        try {
            if (considerSimilar.contains(Pair(antlrNode.name, psiNode.name))) return
            if (antlrNode.valuableChildrenCount != psiNode.valuableChildrenCount) throw Exception()
            var antlrNextToCheck = antlrNode.nextValuableChild(0)
            var psiNextToCheck = psiNode.nextValuableChild(0)
            while (!(antlrNextToCheck == null && psiNextToCheck == null)) {
                if (antlrNextToCheck == null || psiNextToCheck == null) throw Exception()

                compareTrees(antlrNextToCheck, psiNextToCheck, errors)

                antlrNextToCheck = antlrNode.nextValuableChild(antlrNextToCheck.index + 1);
                psiNextToCheck = psiNode.nextValuableChild(psiNextToCheck.index + 1);
            }
        } catch (ex: Exception) {
            errors.add(ComparisonError(antlrNode.name, psiNode.name, antlrNode, psiNode))
        }
    }
}