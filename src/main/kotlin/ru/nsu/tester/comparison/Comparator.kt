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
import kotlin.math.pow

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

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

data class ComparisonResult(val errors: MutableList<ComparisonError>?) {
    private val beta: Double = 1.0
    var falseCases: Int = 0
    var totalOutput: Int = 0
    var totalGold: Int = 0

    private fun countPrecision() : Double {
        if (totalOutput > 0) {
            return (totalOutput - falseCases) * 1.0 / totalOutput
        }
        return 0.0
    }
    private fun countRecall() : Double {
        if (totalGold > 0) {
            return (totalGold - falseCases) * 1.0 / totalGold
        }
        return 0.0
    }
    private fun countFScore() : Double {
        val precision = countPrecision()
        val recall = countRecall()
        return (1 + beta.pow(2)) * precision * recall / (beta.pow(2) * precision + recall)
    }

    fun isCorrect() = errors == null || errors.isEmpty()
    fun output() {
        if (isCorrect()) println("Compared successfully")
        else {
            println("Comparison errors detected:")
            errors!!.forEach { println(it) }
            println()
            println("Precision: ${countPrecision().format(2)}")
            println("Recall: ${countRecall().format(2)}")
            println("F-score: ${countFScore().format(2)}")
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

        // TODO: assert psi doesn't have errors
        val psiTree = PsiTreeBuilder.build(parsed.treeElement!!)
        val errors = mutableListOf<ComparisonError>()
        val result = ComparisonResult(errors)
        compareTrees(AntlrTreeWrapper(antlrTree), PsiTreeWrapper(psiTree), result)

        return result
    }

    private fun compareTrees(antlrNode: TreeWrapper, psiNode: TreeWrapper, result: ComparisonResult) {
        try {
            if (considerSimilar.contains(Pair(antlrNode.name, psiNode.name))) return
            if (antlrNode.textRange != psiNode.textRange) result.falseCases++
            if (antlrNode.valuableChildrenCount != psiNode.valuableChildrenCount) throw Exception()
            var antlrNextToCheck = antlrNode.nextValuableChild(0)
            var psiNextToCheck = psiNode.nextValuableChild(0)
            while (!(antlrNextToCheck == null && psiNextToCheck == null)) {
                result.totalOutput++;
                result.totalGold++;
                if (antlrNextToCheck == null) {
                    result.totalOutput--;
                    throw Exception()
                }
                if (psiNextToCheck == null) {
                    result.totalGold--;
                    throw Exception()
                }

                compareTrees(antlrNextToCheck, psiNextToCheck, result)

                antlrNextToCheck = antlrNode.nextValuableChild(antlrNextToCheck.index + 1);
                psiNextToCheck = psiNode.nextValuableChild(psiNextToCheck.index + 1);
            }
        } catch (ex: Exception) {
            result.errors?.add(ComparisonError(antlrNode.name, psiNode.name, antlrNode, psiNode))
        }
    }
}