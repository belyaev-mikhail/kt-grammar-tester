package ru.nsu.tester

import ru.nsu.tester.comparison.Comparator
import ru.nsu.tester.gui.AnalysisRenderer
import ru.nsu.tester.parsing.ParsingOverview
import java.awt.EventQueue
import java.io.File

fun main(args: Array<String>) {
    val dir = File(if (args.isNotEmpty()) args[0] else ".")
    assert(dir.exists() && dir.isDirectory)

    val resultFile = File("kotoed-result.md")
    resultFile.writeText("File | Precision | Recall | F-score\n")
    resultFile.appendText(":----:|:----:|:----:|:----:\n")

    var correctCount = 0
    var totalCount = 0
    dir.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach {
                println("${it.name}:")
                val result = ParsingOverview.parse(it.inputStream())
                result.output()
                if (result.isCorrect()) {
                    val compareResult = Comparator.inspectTree(it.path, result.root!!)
                    compareResult.consoleOutput()
                    resultFile.appendText(compareResult.fileOutput(it) + "\n")
                    if (compareResult.isCorrect()) correctCount++
                    /* Uncomment to view erroneous subtree */
                    /*
                    else {
                        EventQueue.invokeLater({
                            run { AnalysisRenderer(compareResult.errors!!.first()).display() }
                        })
                    }
                    */
                }
                totalCount++
                println()
            }

    println("Correct: $correctCount / $totalCount")
}