package ru.nsu.util

import java.io.File

private const val KOTOED_PROJECT_PATH = "testData/kotoed-project"
private const val FUZZER_PROJECT_PATH = "testData/fuzzer-project/fuzzer/src/test/resources"
private const val KT_COMPILER_PROJECT_PATH = "testData/kt-compiler-project/compiler/testData/psi"
private const val LOCAL_PROJECT_PATH = "testData/test"

private const val KOTOED_RESULTS_PATH = "doc/results/kotoed-result.md"
private const val FUZZER_RESULTS_PATH = "doc/results/fuzzer-result.md"
private const val KT_COMPILER_RESULTS_PATH = "doc/results/kt-compiler-result.md"
private const val LOCAL_RESULTS_PATH = "doc/results/local-result.md"

private const val KOTOED_PROJECT_PREFIX = "https://bitbucket.org/vorpal-research/kotoed/src/tip"
private const val FUZZER_PROJECT_PREFIX = "https://github.com/ItsLastDay/KotlinFuzzer/tree/master/fuzzer/src/test/resources"
private const val KT_COMPILER_PROJECT_PREFIX = "https://github.com/JetBrains/kotlin/tree/master/compiler/testData/psi"

sealed class Configuration(
        resultPath: String,
        val testProjectLocalPath: String,
        val testProjectWebPrefix: String
) {
    object KOTOED: Configuration(KOTOED_RESULTS_PATH, KOTOED_PROJECT_PATH, KOTOED_PROJECT_PREFIX)
    object FUZZER: Configuration(FUZZER_RESULTS_PATH, FUZZER_PROJECT_PATH, FUZZER_PROJECT_PREFIX)
    object KT_COMPILER: Configuration(KT_COMPILER_RESULTS_PATH, KT_COMPILER_PROJECT_PATH, KT_COMPILER_PROJECT_PREFIX)
    class Local(resultPath: String = LOCAL_RESULTS_PATH, projectPath: String = LOCAL_PROJECT_PATH, webPrefix: String = "")
        : Configuration(resultPath, projectPath, webPrefix)

    val localPrefix = System.getProperty("user.dir").replace("\\", "/") + "/"
    val resultFile = File(resultPath)
    val dir: File
        get() {
            val file = File(localPrefix + testProjectLocalPath)
            assert(file.exists() && file.isDirectory)
            return file
        }

    fun initResultFile() : File {
        resultFile.writeText("File | Precision | Recall | F-score\n")
        resultFile.appendText(":----:|:----:|:----:|:----:\n")

        return resultFile
    }
}

