package ru.nsu.tester

import ru.nsu.tester.comparison.Comparator
import ru.nsu.tester.parsing.ParsingOverview import ru.nsu.tester.parsing.ParsingResult
import ru.nsu.util.Configuration
import java.io.File

fun main(args: Array<String>) {
    val cfg = Configuration.KT_COMPILER
    val resultFile = cfg.getResultFile()
    val dir = cfg.getDirectory()

    var correctCount = 0
    var totalCount = 0
    dir.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach {
                println("${it.name}:")

                var result: ParsingResult? = null
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
                if (compareResult != null) resultFile.appendText(compareResult.fileOutput(it, cfg) + "\n")
                if (compareResult != null && compareResult.isCorrect()) correctCount++
                /* Uncomment to view erroneous subtree */
                /*
                else if (compareResult != null)
                    EventQueue.invokeLater {
                        run { AnalysisRenderer(compareResult.errors!!.first()).display() }
                    }
                */
                totalCount++
                println()
            }

    println("Correct: $correctCount / $totalCount")
}