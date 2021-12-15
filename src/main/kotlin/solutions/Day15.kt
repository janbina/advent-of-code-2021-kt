package solutions

import utils.Point2D
import java.io.BufferedReader
import java.util.*

class Day15(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: CaveMap by lazy {
        val map = mutableMapOf<Point2D, Int>()

        inputReader.useLines { lines ->
            lines.forEachIndexed { y, line ->
                line.map { it.digitToInt() }.forEachIndexed { x, risk ->
                    map[Point2D(x, y)] = risk
                }
            }
        }

        CaveMap(map)
    }

    override fun solvePart1(): Int {
        val caveMap = input
        val start = caveMap.topLeft
        val end = caveMap.bottomRight

        return findLowestRiskPath(start, end) { caveMap.riskForPoint(it, 1) }
    }

    override fun solvePart2(): Int {
        val caveMap = input
        val start = caveMap.topLeft
        val end = caveMap.bottomRight

        return findLowestRiskPath(start, end) { caveMap.riskForPoint(it, 5) }
    }

    /**
     * Finds path from [start] to [end] in [riskMap] with the lowest possible risk
     * and @returns that risk.
     */
    private fun findLowestRiskPath(
        start: Point2D,
        end: Point2D,
        riskForPoint: (Point2D) -> Int?,
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
                riskForPoint(point)?.let { pointRisk ->
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

    private data class QueueEntry(
        val point: Point2D,
        val risk: Int,
    )

    private class CaveMap(
        private val risks: Map<Point2D, Int>,
    ) {
        val minX = 0
        val minY = 0
        val maxX = risks.keys.maxOf { it.x }
        val maxY = risks.keys.maxOf { it.y }
        val topLeft = Point2D(minX, minY)
        val bottomRight = Point2D(maxX, maxY)

        fun riskForPoint(point: Point2D, mapDuplication: Int = 1): Int? {
            return risks[point]
        }
    }
}
