package ru.nsu.tester.comparison

import ru.nsu.tester.comparison.wrapper.TreeWrapper
import java.io.File

val LOCAL_FUZZLER_PROJECT_PREFIX = "/home/shadrina/tests/KotlinFuzzer/fuzzer/src/test/resources"
val FUZZLER_PROJECT_PREFIX = "https://github.com/ItsLastDay/KotlinFuzzer/tree/master/fuzzer/src/test/resources"

val LOCAL_KOTOED_PROJECT_PREFIX = "/home/shadrina/tests/kotoed"
val KOTOED_PROJECT_PREFIX = "https://bitbucket.org/vorpal-research/kotoed/src/f50fa0290fb1f33a303873ac62f9b94c513abde9"

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

data class ComparisonError(
        val antlrRule: String?,
        val psiRule: String?,
        val antlrTree: TreeWrapper,
        val psiTree: TreeWrapper) {

    var weight: Int = 0

    override fun toString(): String {
        return "Difference found in ANTLR: $antlrRule / PSI: $psiRule"
    }
}

data class ComparisonResult(val errors: MutableList<ComparisonError>?) {
    var totalOutput: Int = 0
    var totalGold: Int = 0
    var totalErrorWeight: Int = 0

    private var precision: Double = .0
    private var recall: Double = .0
    private var fScore: Double = .0
    private val accuracy: Int = 3

    private fun countTotalErrorWeight() {
        if (totalErrorWeight == 0) errors?.forEach { totalErrorWeight += it.weight }
    }
    private fun countPrecision() {
        if (totalOutput > 0)
            precision = (totalOutput - totalErrorWeight) * 1.0 / totalOutput

    }
    private fun countRecall() {
        if (totalGold > 0)
            recall = (totalGold - totalErrorWeight) * 1.0 / totalGold
    }
    private fun countFScore() {
        countTotalErrorWeight()
        countPrecision()
        countRecall()
        fScore = 2 * (precision * recall) * 1.0 / (precision + recall)
    }

    fun isCorrect() = errors == null || errors.isEmpty()
    fun consoleOutput() {
        if (isCorrect()) println("Compared successfully")
        else {
            println("Comparison errors detected:")
            errors!!.forEach { println(it) }
            println()

            countFScore()
            println("Precision: ${precision.format(accuracy)}")
            println("Recall: ${recall.format(accuracy)}")
            println("F-score: ${fScore.format(accuracy)}")
        }
    }
    fun fileOutput(file: File) : String {
        countFScore()
        val separator = " | "
        val link = file.absolutePath
                .replace("\\", "/")
                .replace(LOCAL_KOTOED_PROJECT_PREFIX, KOTOED_PROJECT_PREFIX)
        var output = '[' + file.name + ']' + '(' + link + ')'
        output += separator
        output += precision.format(accuracy) + separator
        output += recall.format(accuracy) + separator
        output += fScore.format(accuracy)
        return output
    }
}