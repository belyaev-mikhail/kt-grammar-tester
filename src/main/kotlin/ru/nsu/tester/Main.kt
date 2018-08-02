package ru.nsu.tester

import ru.nsu.tester.comparison.Comparator
import ru.nsu.tester.comparison.ComparisonResult
import ru.nsu.tester.parsing.ParsingOverview
import ru.nsu.tester.parsing.ParsingResult
import ru.nsu.util.Configuration
import ru.nsu.util.ResultAnalyzer

fun main(args: Array<String>) {
    val cfg = Configuration.KT_COMPILER
    val resultFile = cfg.initResultFile()
    val dir = cfg.dir

    val results = mutableListOf<ComparisonResult>()

    dir.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach {
                println("${it.name}:")

                var result: ParsingResult?
                try {
                    result = ParsingOverview.parse(it.inputStream())
                } catch(ex: Exception) {
                    println("ERROR: ANTLR throws Exception")
                    return@forEach
                }
                result.output()

                val antlrTree = if (result.isCorrect()) result.root!! else null
                val compareResult = Comparator.inspectTree(it.path, antlrTree)
                compareResult?.consoleOutput()
                if (compareResult != null) {
                    resultFile.appendText(compareResult.fileOutput(it, cfg) + "\n")
                    results.add(compareResult)
                }
                /* Uncomment to view erroneous subtree */
                /*
                else if (compareResult != null)
                    EventQueue.invokeLater {
                        run { AnalysisRenderer(compareResult.errors!!.first()).display() }
                    }
                */
                println()
            }

    ResultAnalyzer(cfg).gaugeChanges(results)
}