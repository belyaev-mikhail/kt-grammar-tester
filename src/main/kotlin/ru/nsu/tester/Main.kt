package ru.nsu.tester

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import ru.nsu.tester.comparison.Comparator
import ru.nsu.tester.comparison.ComparisonResult
import ru.nsu.tester.gui.AnalysisRenderer
import ru.nsu.tester.parsing.ParsingOverview
import ru.nsu.tester.parsing.ParsingResult
import ru.nsu.util.Configuration
import ru.nsu.util.ResultAnalyzer
import java.awt.EventQueue

class Args(parser: ArgParser) {
    val ktCompiler by parser.flagging(help = "Run on kotlin compiler test cases")
    val kotoed by parser.flagging(help = "Run on kotoed project")
    val fuzzer by parser.flagging(help = "Run on fuzzer data")
    val results by parser.storing(help = "Local result file path").default<String?>(null)
    val dir by parser.storing(help = "Local test directory").default<String?>(null)

    val display by parser.flagging(help = "Display differences GUI").default(false)

    val cfg by lazy {
        when {
            dir != null && results != null -> Configuration.Local(results!!, dir!!)
            dir != null -> Configuration.Local(projectPath = dir!!)
            kotoed -> Configuration.KOTOED
            fuzzer -> Configuration.FUZZER
            else -> Configuration.KT_COMPILER
        }
    }
}

fun main(args: Array<String>) {
    val args = ArgParser(args).parseInto(::Args)

    val cfg = args.cfg
    val resultFile = cfg.initResultFile()
    val dir = cfg.dir

    val results = mutableListOf<ComparisonResult>()

    dir.walkTopDown()
            .filter { it.extension == "kt" }
            .sortedBy { it.absolutePath }
            .forEach {
                println("${it.name}:")

                val result: ParsingResult?
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

                if (compareResult?.isCorrect() == false && args.display)
                    EventQueue.invokeLater {
                        run { AnalysisRenderer(compareResult.errors!!.first()).display() }
                    }

                println()
            }

    ResultAnalyzer(cfg).gaugeChanges(results)
}