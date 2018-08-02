package ru.nsu.tester.comparison

import ru.nsu.tester.comparison.wrapper.TreeWrapper
import ru.nsu.util.Configuration
import java.io.File
import java.io.Serializable

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

data class ComparisonResult(@Transient val errors: MutableList<ComparisonError>?) : Serializable {
    @Transient var totalOutput: Int = 0
    @Transient var totalGold: Int = 0
    @Transient var totalErrorWeight: Int = 0

    var precision: Double = .0
    var recall: Double = .0
    var fScore: Double = .0
    @Transient private val accuracy: Int = 3

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
    fun fileOutput(file: File, cfg: Configuration) : String {
        countFScore()
        val separator = " | "
        val link = file.absolutePath
                .replace("\\", "/")
                .replace(cfg.localPrefix + cfg.testProjectLocalPath, cfg.testProjectWebPrefix)
        var output = '[' + file.name + ']' + '(' + link + ')'
        output += separator
        output += precision.format(accuracy) + separator
        output += recall.format(accuracy) + separator
        output += fScore.format(accuracy)
        return output
    }
}