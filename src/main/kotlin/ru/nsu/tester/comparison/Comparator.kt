package ru.nsu.tester.comparison

import org.jetbrains.kootstrap.FooBarCompiler
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.addKotlinSourceRoot
import org.jetbrains.kotlin.psi.KtPsiFactory
import ru.nsu.tester.comparison.deserialization.PsiTreeBuilder
import ru.nsu.tester.comparison.wrapper.*
import ru.nsu.gen.KotlinParser.KotlinFileContext

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
        val nodesNumber = (0 until this.childrenCount).sumBy { this.getChild(it).countNodes() }
        return nodesNumber + 1
    }

    private fun compareTrees(antlrNode: TreeWrapper, psiNode: TreeWrapper, result: ComparisonResult) {
        try {
            var normalizedAntlrNode: TreeWrapper? = null
            var normalizedPsiNode: TreeWrapper? = null

            // If nodes describe different code, there is no point in further checking
            if (antlrNode.textRange != psiNode.textRange) {
                println(antlrNode.textRange)
                println(psiNode.textRange)
                throw Exception()
            }
            // If nodes have different structure, try to normalize them
            if (antlrNode.valuableChildrenCount != psiNode.valuableChildrenCount) {
                if (psiNode.isInSpecialChomskyForm()) normalizedAntlrNode = TreeNormalizer.normalize(antlrNode)
                else throw Exception()

                // Sometimes we need to normalize PSI (?)
                // normalizedPsiNode = TreeNormalizer.normalize(psiNode)
            }

            var antlrNextToCheck = (normalizedAntlrNode ?: antlrNode).nextValuableChild(0)
            var psiNextToCheck = (normalizedPsiNode ?: psiNode).nextValuableChild(0)
            while (!(antlrNextToCheck == null && psiNextToCheck == null)) {
                if (antlrNextToCheck == null || psiNextToCheck == null) throw Exception()

                compareTrees(antlrNextToCheck, psiNextToCheck, result)

                antlrNextToCheck = (normalizedAntlrNode ?: antlrNode).nextValuableChild(antlrNextToCheck.index + 1)
                psiNextToCheck = (normalizedPsiNode ?: psiNode).nextValuableChild(psiNextToCheck.index + 1)
            }
        } catch (ex: Exception) {
            val errorWeight = psiNode.countNodes()
            val error = ComparisonError(antlrNode.name, psiNode.name, antlrNode, psiNode)
            error.weight = errorWeight
            result.errors?.add(error)
        }
    }
}