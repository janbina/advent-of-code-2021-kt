package solutions

import java.io.BufferedReader
import java.util.*

class Day10(
    inputReader: BufferedReader,
) : Day<Int, Long>() {

    private val input: List<String> by lazy {
        inputReader.transformLines { it }
    }

    override fun solvePart1(): Int {
        return input
            .map { evaluateLine(it) }
            .filterIsInstance<LineResult.Corrupted>()
            .sumOf { it.firstIllegalCharacter.illegalScore }
    }

    override fun solvePart2(): Long {
        val scores = input
            .map { evaluateLine(it) }
            .filterIsInstance<LineResult.Incomplete>()
            .map { it.completion.completionScore }
            .sorted()
        return scores[scores.size / 2]
    }

    private fun evaluateLine(line: String): LineResult {
        val stack = ArrayDeque<Char>()

        line.forEach { c ->
            if (c.isOpeningBracket) {
                stack.offerLast(c)
            } else if (c.isClosingBracket) {
                if (stack.isEmpty()) return LineResult.Corrupted(c)
                val opening = stack.removeLast()
                if (opening != c.matchingBracket) return LineResult.Corrupted(c)
            } else {
                return LineResult.Corrupted(c)
            }
        }

        return if (stack.isEmpty()) {
            LineResult.Ok
        } else {
            LineResult.Incomplete(
                stack.reversed().map { it.matchingBracket }.joinToString(separator = "")
            )
        }
    }

    private val String.completionScore: Long
        get() = fold(0L) { acc, c -> acc * 5 + c.completionScore }

    private val Char.isOpeningBracket: Boolean
        get() = this in OpeningBrackets

    private val Char.isClosingBracket: Boolean
        get() = this in ClosingBrackets

    private val Char.matchingBracket: Char
        get() = when (this) {
            '(' -> ')'
            ')' -> '('
            '[' -> ']'
            ']' -> '['
            '{' -> '}'
            '}' -> '{'
            '<' -> '>'
            '>' -> '<'
            else -> error("Not a bracket $this")
        }

    private val Char.illegalScore: Int
        get() = when (this) {
            ')' -> 3
            ']' -> 57
            '}' -> 1197
            '>' -> 25137
            else -> 0
        }

    private val Char.completionScore: Int
        get() = when (this) {
            ')' -> 1
            ']' -> 2
            '}' -> 3
            '>' -> 4
            else -> 0
        }

    private sealed class LineResult {
        object Ok : LineResult()

        class Corrupted(
            val firstIllegalCharacter: Char,
        ) : LineResult()

        class Incomplete(
            val completion: String,
        ) : LineResult()
    }

    companion object {
        private val OpeningBrackets = setOf('(', '[', '{', '<')
        private val ClosingBrackets = setOf(')', ']', '}', '>')
    }
}
