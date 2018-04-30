package ru.nsu.tester.parsing

import org.antlr.v4.runtime.*
import ru.nsu.gen.KotlinLexer
import ru.nsu.gen.KotlinParser
import ru.nsu.gen.KotlinParser.KotlinFileContext
import java.io.FileInputStream
import java.util.*

data class ParsingError(val msg: String?, val line: Int, val charPosition: Int) {
    override fun toString(): String {
        return "Line $line:$charPosition $msg"
    }
}

data class ParsingResult(val root: KotlinFileContext?, val errors: List<ParsingError>) {
    fun isCorrect() = errors.isEmpty() && root != null
    fun output() {
        if (isCorrect()) println("Parsed successfully")
        else {
            println("Grammar errors detected:")
            errors.forEach { println(it) }
        }
    }
}

object ParsingOverview {
    fun parse(ktfile: FileInputStream) : ParsingResult {
        val errors = LinkedList<ParsingError>()
        val errorListener = object : BaseErrorListener() {
            override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
                errors.add(ParsingError(msg, line, charPositionInLine))
            }
        }
        val lexer = KotlinLexer(ANTLRInputStream(ktfile))
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)
        val parser = KotlinParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)
        val root = parser.kotlinFile()
        return ParsingResult(root, errors)
    }
}