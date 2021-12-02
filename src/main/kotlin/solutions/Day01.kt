package solutions

import java.io.BufferedReader

class Day01(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: List<Int> by lazy {
        inputReader.transformLines { it.toInt() }
    }

    override fun solvePart1(): Int {
        return input.countIncreases()
    }

    override fun solvePart2(): Int {
        return input.windowed(
            size = 3,
            transform = { it.sum() },
        ).countIncreases()
    }

    private fun Iterable<Int>.countIncreases(): Int {
        return windowed(
            size = 2,
            transform = { it[0] < it[1] }
        ).count { it }
    }
}
