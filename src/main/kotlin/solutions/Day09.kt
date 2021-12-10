package solutions

import utils.Point2D
import java.io.BufferedReader
import kotlin.collections.ArrayDeque

class Day09(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: HeightMap by lazy {
        val points = mutableMapOf<Point2D, Int>()
        inputReader.useLines { lines ->
            lines.forEachIndexed { y, line ->
                line.forEachIndexed { x, c ->
                    points[Point2D(x, y)] = c.digitToInt()
                }
            }
        }
        points
    }

    override fun solvePart1(): Int {
        return input.entries.sumOf { (point, height) ->
            val neighborHeights = point.neighbors().mapNotNull { input[it] }
            if (neighborHeights.all { it > height }) {
                height + 1
            } else {
                0
            }
        }
    }

    override fun solvePart2(): Int {
        val basins = mutableListOf<Set<Point2D>>()
        val explored = mutableSetOf<Point2D>()

        while (true) {
            val start = input.entries
                .filter { it.value < 9 }
                .firstOrNull { it.key !in explored }
                ?.key ?: break
            val basin = findBasin(input, start)
            basins.add(basin)
            explored.addAll(basin)
        }

        return basins.map { it.size }
            .sortedDescending()
            .take(3)
            .fold(1) { acc, i -> acc * i }
    }

    private fun findBasin(map: HeightMap, start: Point2D): Set<Point2D> {
        val height = map[start] ?: return emptySet()
        if (height == 9) return emptySet()

        val explored = mutableSetOf<Point2D>()
        val queue = ArrayDeque<Point2D>()

        queue.addLast(start)
        explored.add(start)
        while (queue.isNotEmpty()) {
            val x = queue.removeFirst()
            x.neighbors().filter { (map[it] ?: 9) < 9 }.forEach { neighbor ->
                if (explored.add(neighbor)) {
                    queue.addLast(neighbor)
                }
            }
        }

        return explored
    }
}

typealias HeightMap = Map<Point2D, Int>
