package solutions

import java.io.BufferedReader

class Day14(
    inputReader: BufferedReader,
): Day<Long, Long>() {

    private val input: Input by lazy {
        val template = inputReader.readLine()
        val insertionRules = mutableMapOf<String, Char>()

        inputReader.useLines { lines ->
            lines.filter { it.contains(" -> ") }.forEach { line ->
                val (a, b) = line.split(" -> ")
                insertionRules[a] = b.single()
            }
        }

        Input(template, insertionRules)
    }

    override fun solvePart1(): Long {
        val charCounts = makePolymer(input.template, input.insertionRules, 10)
        return charCounts.maxOf { it.value } - charCounts.minOf { it.value }
    }

    override fun solvePart2(): Long {
        val charCounts = makePolymer(input.template, input.insertionRules, 40)
        return charCounts.maxOf { it.value } - charCounts.minOf { it.value }
    }

    private fun makePolymer(template: String, rules: Map<String, Char>, rounds: Int): Map<Char, Long> {
        val pairCount = template.toPairCount()
        val result = (0 until rounds).fold(pairCount) { acc, _ -> acc.applyRules(rules) }
        return result.toCharCount(input.template)
    }

    private fun Map<String, Long>.applyRules(rules: Map<String, Char>): Map<String, Long> {
        val result = mutableMapOf<String, Long>()

        forEach { (pair, num) ->
            val middle = rules[pair]
            if (middle != null) {
                result.increment(pair[0].toString() + middle, num)
                result.increment(middle.toString() + pair[1], num)
            } else {
                result.increment(pair, num)
            }
        }

        return result
    }

    private fun String.toPairCount(): Map<String, Long> {
        return windowed(2)
            .groupingBy { it }
            .eachCount()
            .mapValues { it.value.toLong() }
    }

    private fun Map<String, Long>.toCharCount(initialTemplate: String): Map<Char, Long> {
        val result = mutableMapOf<Char, Long>()

        result.increment(initialTemplate.first(), 1)
        this.forEach { (pair, num) ->
            result.increment(pair[1], num)
        }

        return result
    }

    private fun <K> MutableMap<K, Long>.increment(key: K, increment: Long) {
        this[key] = increment + (this[key] ?: 0)
    }

    private class Input(
        val template: String,
        val insertionRules: Map<String, Char>,
    )
}
