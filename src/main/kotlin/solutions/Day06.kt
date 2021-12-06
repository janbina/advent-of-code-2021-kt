package solutions

import java.io.BufferedReader

class Day06(
    inputReader: BufferedReader,
): Day<Long, Long>() {

    private val input: List<Int> by lazy {
        inputReader.transformLines { line ->
            line.split(",").map(String::toInt)
        }.first()
    }

    override fun solvePart1(): Long {
        return input.sumOf { simulateFish(it, 80) }
    }

    override fun solvePart2(): Long {
        return input.sumOf { simulateFish(it, 256) }
    }

    private val cache = mutableMapOf<Pair<Int, Int>, Long>()

    private fun simulateFish(daysUntilNew: Int, daysRemaining: Int): Long {
        val cached = cache[daysUntilNew to daysRemaining]
        if (cached != null) return cached

        return if (daysRemaining == 0) {
            1
        } else if (daysUntilNew == 0) {
            simulateFish(6, daysRemaining - 1) + simulateFish(8, daysRemaining - 1)
        } else {
            simulateFish(daysUntilNew - 1, daysRemaining - 1)
        }.also {
            cache[daysUntilNew to daysRemaining] = it
        }
    }
}
