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

        val antlrNode = AntlrTreeWrapper(antlrTree)
        val psiNode = PsiTreeWrapper(psiTree)
        result.totalOutput = antlrNode.countNodes()
        result.totalGold = psiNode.countNodes()
        compareTrees(antlrNode, psiNode, result)

        return result
    }

    private fun TreeWrapper.countNodes() : Int {
        var nodesNumber = 0
        for (i in 0 until this.childrenCount) {
            nodesNumber += this.getChild(i).countNodes()
        }
        return nodesNumber + 1
    }

    private fun TreeWrapper.isInChomskyForm() : Boolean {
        if (childrenCount <= 3) {
            // If there are 3 children, the middle one should be a terminal
            if (childrenCount == 3 && this.getChild(1).childrenCount != 0) return false
            for (i in 0 until childrenCount) {
                if (!this.getChild(i).isInChomskyForm()) return false
            }
            return true
        }
        return false
    }

    private fun compareTrees(antlrNode: TreeWrapper, psiNode: TreeWrapper, result: ComparisonResult) {
        try {
            var normalizedAntlrNode = antlrNode
            if (antlrNode.valuableChildrenCount != psiNode.valuableChildrenCount) {
                if (antlrNode.textRange == psiNode.textRange
                        && psiNode.isInChomskyForm() && !antlrNode.isInChomskyForm()) {
                    normalizedAntlrNode = TreeNormalizer.normalize(antlrNode)
                } else throw Exception()
            }
            if (normalizedAntlrNode.textRange != psiNode.textRange) {
                println(normalizedAntlrNode.textRange)
                println(psiNode.textRange)
                throw Exception()
            }

            var antlrNextToCheck = normalizedAntlrNode.nextValuableChild(0)
            var psiNextToCheck = psiNode.nextValuableChild(0)
            while (!(antlrNextToCheck == null && psiNextToCheck == null)) {
                if (antlrNextToCheck == null || psiNextToCheck == null) throw Exception()

                compareTrees(antlrNextToCheck, psiNextToCheck, result)

                antlrNextToCheck = normalizedAntlrNode.nextValuableChild(antlrNextToCheck.index + 1)
                psiNextToCheck = psiNode.nextValuableChild(psiNextToCheck.index + 1)
            }
        } catch (ex: Exception) {
            val errorWeight = psiNode.countNodes()
            val error = ComparisonError(antlrNode.name, psiNode.name, antlrNode, psiNode)
            error.weight = errorWeight
            result.errors?.add(error)
        }
    }
}