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
        val mapping = signalPatterns.createMapping()
        return outputValue.map {
            mapping[it] ?: error("No mapping for $it")
        }.joinToString("").toInt()
    }

    private fun List<Set<Char>>.createMapping(): Map<Set<Char>, Int> {
        return mutableMapOf<Int, Set<Char>>().apply {
            this[1] = first { it.size == 2 }
            this[4] = first { it.size == 4 }
            this[7] = first { it.size == 3 }
            this[8] = first { it.size == 7 }
            this[9] = first { it.size == 6 && it.containsAll(this[4]!!) }
            this[6] = first { it.size == 6 && !it.containsAll(this[7]!!) }
            this[0] = first { it.size == 6 && it != this[6] && it != this[9] }
            this[3] = first { it.size == 5 && it.containsAll(this[1]!!) }
            this[2] = first { it.size == 5 && it.intersect(this[4]!!).size == 2 }
            this[5] = first { it.size == 5 && it != this[3] && it != this[2] }
        }.entries.associateBy({ it.value }) { it.key }
    }

    private data class Entry(
        val signalPatterns: List<Set<Char>>,
        val outputValue: List<Set<Char>>,
    )
}
