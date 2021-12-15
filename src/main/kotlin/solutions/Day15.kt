package solutions

import utils.Point2D
import java.io.BufferedReader
import java.util.*

class Day15(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: Map<Point2D, Int> by lazy {
        val map = mutableMapOf<Point2D, Int>()

        inputReader.useLines { lines ->
            lines.forEachIndexed { y, line ->
                line.map { it.digitToInt() }.forEachIndexed { x, risk ->
                    map[Point2D(x, y)] = risk
                }
            }
        }

        map
    }

    override fun solvePart1(): Int {
        val riskMap = input
        val start = Point2D(x = 0, y = 0)
        val end = Point2D(
            x = riskMap.keys.maxOf { it.x },
            y = riskMap.keys.maxOf { it.y },
        )

        return findLowestRiskPath(riskMap, start, end)
    }

    override fun solvePart2(): Int {
        return 0
    }

    /**
     * Finds path from [start] to [end] in [riskMap] with the lowest possible risk
     * and @returns that risk.
     */
    private fun findLowestRiskPath(
        riskMap: Map<Point2D, Int>,
        start: Point2D,
        end: Point2D,
    ): Int {
        val queue = PriorityQueue<QueueEntry>(compareBy({ -(it.point.x + it.point.y) }, { it.risk }))
        val visited = mutableMapOf<Point2D, Int>()
        queue.add(QueueEntry(start, 0))
        visited[start] = 0
        var lowestPossibleRisk = Int.MAX_VALUE

        while (queue.isNotEmpty()) {
            val top = queue.poll()
            if (top.risk >= lowestPossibleRisk) continue
            val next = top.point.neighbors().mapNotNull { point ->
                riskMap[point]?.let { pointRisk ->
                    QueueEntry(point, top.risk + pointRisk)
                }
            }
            next.forEach { nextEntry ->
                if (nextEntry.point == end) {
                    if (nextEntry.risk < lowestPossibleRisk) {
                        lowestPossibleRisk = nextEntry.risk
                    }
                } else {
                    val prevRisk = visited[nextEntry.point]
                    if (prevRisk == null || prevRisk > nextEntry.risk) {
                        queue.add(nextEntry)
                        visited[nextEntry.point] = nextEntry.risk
                    }
                }
            }
        }

        return lowestPossibleRisk
    }

    data class QueueEntry(
        val point: Point2D,
        val risk: Int,
    )
}
