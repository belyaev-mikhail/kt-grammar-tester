package ru.nsu.gen

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Token
import java.util.*

abstract class KotlinLexerBase : Lexer {
    constructor(stream: CharStream): super(stream)
    constructor(): super()

    val tokenQueue: Queue<Token> = ArrayDeque<Token>()

    override fun nextToken(): Token {
        super.nextToken()
        return tokenQueue.remove()
    }

    override fun getToken(): Token {
        super.getToken()
        return tokenQueue.element()
    }

    override fun emit(token: Token?) {
        tokenQueue += token
    }

    fun split(at: Int, newToken1: Int, newToken2: Int) {
        val t1Text = text.substring(0, at)
        val t2Text = text.substring(at)

        val t1 = _factory.create(_tokenFactorySourcePair, newToken1, t1Text,
                _channel, _tokenStartCharIndex, charIndex - 1,
                _tokenStartLine, _tokenStartCharPositionInLine)

        _tokenStartCharIndex += t1Text.length
        _tokenStartCharPositionInLine += t1Text.length

        emit(t1)

        text = t2Text
        type = newToken2
    }

}