package solutions

import utils.Point2D
import java.io.BufferedReader

class Day11(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: List<List<Int>> by lazy {
        inputReader.transformLines { line ->
            line.map { it.digitToInt() }
        }
    }

    override fun solvePart1(): Int {
        val state = mutableMapOf<Point2D, Int>()
        var totalFlashes = 0
        input.forEachIndexed { y, row ->
            row.forEachIndexed { x, i ->
                state[Point2D(x, y)] = i
            }
        }

        repeat(100) {
            totalFlashes += state.performOneStep()
        }

        return totalFlashes
    }

    override fun solvePart2(): Int {
        val state = mutableMapOf<Point2D, Int>()
        input.forEachIndexed { y, row ->
            row.forEachIndexed { x, i ->
                state[Point2D(x, y)] = i
            }
        }

        var step = 0
        while (true) {
            step++
            state.performOneStep()
            if (state.all { it.value == 0 }) {
                return step
            }
        }
    }

    private fun MutableMap<Point2D, Int>.performOneStep(): Int {
        val flashed = mutableSetOf<Point2D>()

        incrementKeys(keys)

        while (true) {
            val toFlash = entries
                .filter { it.value > 9 }
                .filter { it.key !in flashed }
                .map { it.key }
            flashed.addAll(toFlash)
            incrementKeys(toFlash.flatMap { it.adjacent() })
            if (toFlash.isEmpty()) break
        }

        entries.forEach {
            if (it.value > 9) {
                this[it.key] = 0
            }
        }

        return flashed.count()
    }

    private fun <K> MutableMap<K, Int>.incrementKeys(keys: Collection<K>) {
        keys.forEach { key ->
            if (key in this) {
                this[key] = this[key]!! + 1
            }
        }
    }
}
