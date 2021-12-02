package solutions

import java.io.BufferedReader

class DayXY(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: List<Int> by lazy {
        inputReader.transformLines { it.toInt() }
    }

    override fun solvePart1(): Int {
        return 1
    }

    override fun solvePart2(): Int {
        return 2
    }
}
