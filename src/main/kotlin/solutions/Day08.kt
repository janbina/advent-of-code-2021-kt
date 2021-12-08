package solutions

import java.io.BufferedReader

class Day08(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: List<Entry> by lazy {
        inputReader.transformLines { line ->
            val (signalPatterns, outputValue) = line.split(" | ")
            Entry(
                signalPatterns = signalPatterns.split(" ").map { it.toSet() },
                outputValue = outputValue.split(" ").map { it.toSet() },
            )
        }
    }

    override fun solvePart1(): Int {
        val targetSizes = setOf(2, 3, 4, 7)
        return input.flatMap { it.outputValue }.count {
            it.size in targetSizes
        }
    }

    override fun solvePart2(): Int {
        return input.sumOf {
            it.resolveOutput()
        }
    }

    private fun Entry.resolveOutput(): Int {
        val mapping = signalPatterns.determineMapping()
        return outputValue.map {
            it.toDigit(mapping)
        }.joinToString("").toInt()
    }

    private fun Set<Char>.toDigit(mapping: Map<Char, Char>): Int {
        return when (size) {
            2 -> 1
            3 -> 7
            4 -> 4
            7 -> 8
            5 -> {
                val mapped = map { mapping[it] }
                when {
                    'e' in mapped -> 2
                    'b' in mapped -> 5
                    else -> 3
                }
            }
            6 -> {
                val mapped = map { mapping[it] }
                when {
                    'd' !in mapped -> 0
                    'c' !in mapped -> 6
                    else -> 9
                }
            }
            else -> error("Invalid size")
        }
    }

    private fun List<Set<Char>>.determineMapping(): Map<Char, Char> {
        val mapping = mutableMapOf<Char, Char>()

        val d1 = first { it.size == 2 }
        val d7 = first { it.size == 3 }

        mapping['a'] = (d7 - d1).single()

        val d6 = first { it.size == 6 && it.intersect(d1).size == 1 }

        mapping['c'] = (d1 - d6).single()
        mapping['f'] = d1.intersect(d6).single()

        val d3 = first { it.size == 5 && it.intersect(d1).size == 2 }
        val segDG = d3 - d7
        val d0 = first { it.size == 6 && it.intersect(segDG).size != 2 }

        mapping['d'] = (segDG - d0).single()
        mapping['g'] = segDG.intersect(d0).single()

        val d5 = first { it.size == 5 && mapping['c'] !in it }

        mapping['b'] = (d5 - d3).single()
        mapping['e'] = (d0 - d5 - d1).single()

        return mapping.entries.associateBy({ it.value }) { it.key }
    }

    private data class Entry(
        val signalPatterns: List<Set<Char>>,
        val outputValue: List<Set<Char>>,
    )
}
