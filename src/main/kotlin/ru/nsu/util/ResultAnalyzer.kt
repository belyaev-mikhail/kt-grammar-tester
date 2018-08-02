package ru.nsu.util

import ru.nsu.tester.comparison.ComparisonResult
import java.io.*

class ResultAnalyzer(cfg: Configuration) {
    private val dir = "tmp/"
    private val file = cfg.resultFile.nameWithoutExtension + ".ser"
    private val errorAnswer = Triple(0, 0, 0)

    private var better = 0
    private var same = 0
    private var worse = 0

    fun gaugeChanges(new: List<ComparisonResult>) {
        if (!File(dir + file).exists()) {
            println("No previous result")
            new.save()
        }
        new.analyze()
        new.save()
        print(
                "Better: $better\n" +
                "Same:   $same\n" +
                "Worse:  $worse\n"
        )
    }

    private fun List<ComparisonResult>.save() {
        ObjectOutputStream(FileOutputStream(dir + file)).use { it ->
            it.writeObject(this)
        }
    }

    private fun List<ComparisonResult>.analyze() : Triple<Int, Int, Int> {
        var read: Any? = null
        ObjectInputStream(FileInputStream(dir + file)).use { it ->
            read = it.readObject()
            if (read !is List<*>) {
                println("Deserialization failed")
                return errorAnswer
            }
        }

        val deserialized = read as List<*>
        if (this.size != deserialized.size) {
            println("Different size")
            return errorAnswer
        }

        for (i in 0 until this.size) {
            val new = this[i]
            val prev = deserialized[i] as ComparisonResult

            if (new.fScore > prev.fScore) better++
            if (new.fScore == prev.fScore) same++
            if (new.fScore < prev.fScore) worse++
        }

        return Triple(better, same, worse)
    }
}