package solutions

import utils.Point2D
import java.io.BufferedReader
import java.util.*

class Day15(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: CaveMap by lazy {
        val map = mutableMapOf<Point2D, Int>()

        inputReader.useLines { lines ->
            lines.forEachIndexed { y, line ->
                line.map { it.digitToInt() }.forEachIndexed { x, risk ->
                    map[Point2D(x, y)] = risk
                }
            }
        }

        val width = map.keys.maxOf { it.x } + 1
        val height = map.keys.maxOf { it.y } + 1

        CaveMap(map, width, height)
    }

    override fun solvePart1(): Int {
        val caveMap = input
        val start = Point2D(0, 0)
        val end = Point2D(caveMap.width - 1, caveMap.height - 1)

        return aStar(
            start = start,
            end = end,
            costOfGettingToPoint = {
                caveMap.riskForPoint(it, 1)
            },
            estimateToEnd = {
                end.x - it.x + end.y - it.y
            },
        )
    }

    override fun solvePart2(): Int {
        val caveMap = input
        val start = Point2D(0, 0)
        val end = Point2D(caveMap.width * 5 - 1, caveMap.height * 5 - 1)

        return aStar(
            start = start,
            end = end,
            costOfGettingToPoint = {
                caveMap.riskForPoint(it, 5)
            },
            estimateToEnd = {
                end.x - it.x + end.y - it.y
            },
        )
    }

    /**
     * Finds path from [start] to [end] with the lowest possible risk.
     * Risk for each point of the path is provided by the [riskForPoint] function,
     * which return integer value of the risk or null if it's not possible to move to that point at all.
     * @returns the lowest risk of moving from [start] to [end]
     */
    private fun findLowestRiskPath(
        start: Point2D,
        end: Point2D,
        riskForPoint: (Point2D) -> Int?,
        estimateToEnd: (Point2D) -> Int,
    ): Int {
        val queue = PriorityQueue<QueueEntry>(compareBy { it.risk + estimateToEnd(it.point) } )
        queue.add(QueueEntry(start, 0))

        while (queue.isNotEmpty()) {
            val top = queue.poll()
            val next = top.point.neighbors().mapNotNull { point ->
                riskForPoint(point)?.let { pointRisk ->
                    QueueEntry(point, top.risk + pointRisk)
                }
            }
            next.forEach { nextEntry ->
                if (nextEntry.point == end) {
                    return nextEntry.risk
                } else {
                    val prev = queue.firstOrNull { it.point == nextEntry.point }
                    if (prev == null || prev.risk >= nextEntry.risk) {
                        queue.removeIf { it.point == nextEntry.point }
                        queue.add(nextEntry)
                    }
                }
            }
        }

        return Int.MAX_VALUE
    }

    private fun aStar(
        start: Point2D,
        end: Point2D,
        costOfGettingToPoint: (Point2D) -> Int?,
        estimateToEnd: (Point2D) -> Int,
    ): Int {
        val gScore = mutableMapOf(start to 0)
        val fScore = mutableMapOf(start to estimateToEnd(start))
        val openSet = PriorityQueue<Point2D>(compareBy { fScore[it] ?: Int.MAX_VALUE })
        openSet.add(start)

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()
            if (current == end) return gScore[end] ?: error("No gscore for end")
            current.neighbors().forEach { neighbor ->
                val cost = costOfGettingToPoint(neighbor)
                if (cost != null) {
                    val tentativeGScore = gScore[current]?.plus(cost) ?: Int.MAX_VALUE
                    val currentGScore = gScore[neighbor] ?: Int.MAX_VALUE
                    if (tentativeGScore < currentGScore) {
                        gScore[neighbor] = tentativeGScore
                        fScore[neighbor] = tentativeGScore + estimateToEnd(neighbor)
                        if (neighbor !in openSet) {
                            openSet.add(neighbor)
                        }
                    }
                }
            }
        }

        return Int.MAX_VALUE
    }

    private data class QueueEntry(
        val point: Point2D,
        val risk: Int,
    )

    private class CaveMap(
        private val risks: Map<Point2D, Int>,
        val width: Int,
        val height: Int,
    ) {

        fun riskForPoint(point: Point2D, mapDuplication: Int = 1): Int? {
            val origX = point.x % width
            val origY = point.y % height
            val duplicationX = point.x / width
            val duplicationY = point.y / height

            if (duplicationX >= mapDuplication || duplicationY >= mapDuplication) {
                return null
            }

            val baseRisk = risks[Point2D(origX, origY)] ?: return null

            return (baseRisk + duplicationX + duplicationY - 1) % 9 + 1
        }
    }
}
