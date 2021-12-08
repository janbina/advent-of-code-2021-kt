package solutions

import utils.max
import utils.min
import java.io.BufferedReader
import kotlin.math.absoluteValue

class Day07(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: List<Int> by lazy {
        inputReader.transformLines { it.split(",").map(String::toInt) }.first()
    }

    override fun solvePart1(): Int {
        return input.minOf { alignment ->
            input.sumOf { fuelPart1(it, alignment) }
        }
    }

    override fun solvePart2(): Int {
        return (input.min()..input.max()).minOf { alignment ->
            input.sumOf { fuelPart2(it, alignment) }
        }
    }

    private fun fuelPart1(from: Int, to: Int) = (from - to).absoluteValue

    private fun fuelPart2(from: Int, to: Int): Int {
        val distance = (from - to).absoluteValue
        return distance * (1 + distance) / 2
    }
}
