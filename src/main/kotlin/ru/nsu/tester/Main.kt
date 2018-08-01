package ru.nsu.tester

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import ru.nsu.tester.comparison.Comparator
import ru.nsu.tester.gui.AnalysisRenderer
import ru.nsu.tester.parsing.ParsingOverview
import ru.nsu.tester.parsing.ParsingResult
import ru.nsu.util.Configuration
import java.awt.EventQueue

class Args(parser: ArgParser) {
    val ktCompiler by parser.flagging(help = "Run on kotlin compiler test cases")
    val kotoed by parser.flagging(help = "Run on kotoed project")
    val fuzzer by parser.flagging(help = "Run on fuzzer data")
    val custom by parser.storing(help = "Directory to run on").default<String?>(null)

    val display by parser.flagging(help = "Display differences GUI").default(false)

    val cfg by lazy {
        when {
            custom != null -> Configuration.Local("docs/results/last-result.md", custom!!)
            kotoed -> Configuration.KOTOED
            fuzzer -> Configuration.FUZZER
            else -> Configuration.KT_COMPILER
        }
    }
}

fun main(args: Array<String>) {
    val args = ArgParser(args).parseInto(::Args)

    val cfg = args.cfg
    val dir = cfg.getDirectory()
    val resultFile = cfg.getResultFile()

    var correctCount = 0
    var totalCount = 0
    dir.walkTopDown()
            .filter { it.extension == "kt" }
            .sortedBy { it.absolutePath }
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
                else if (compareResult != null && args.display)
                    EventQueue.invokeLater {
                        run { AnalysisRenderer(compareResult.errors!!.first()).display() }
                    }
                totalCount++
                println()
            }

    println("Correct: $correctCount / $totalCount")
}